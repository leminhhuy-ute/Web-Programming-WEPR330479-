package topicmanagement;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mockito;
import topicmanagement.config.H2DemoDatabaseUpgradeService;

import java.io.File;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.zip.ZipFile;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Kiểm thử cơ chế nâng cấp schema và dữ liệu cho database demo H2:
 * - Chạy hoàn toàn trên database tạm thời do @TempDir quản lý, tự động dọn sạch sau khi test kết thúc.
 * - Tuyệt đối KHÔNG kết nối hay sửa đổi file database demo thật (data/integrated-project.mv.db).
 * - Không phụ thuộc bản ghi cứng (id = 1).
 * - Luôn kiểm tra số dòng thực sự được cập nhật khi thực thi câu lệnh SQL.
 * - Kiểm thử toàn diện:
 *   1. Preflight từ chối nhóm 0 thành viên, không tự ghi giả member_count = 1, database không bị đổi.
 *   2. Preflight từ chối nhóm > 3 thành viên, database không bị đổi.
 *   3. Preflight báo lỗi rõ ràng khi thiếu bảng hoặc cột bắt buộc.
 *   4. Nâng cấp thành công từ database chưa có constraint, tự động tạo backup zip đọc được bằng ZipFile.
 *   5. Nâng cấp thành công từ database cũ có constraint 3–5 (V12), gỡ bỏ ràng buộc cũ và đưa về 1–3.
 *   6. Tính bất biến (idempotent) khi chạy nâng cấp nhiều lần liên tiếp.
 *   7. Mô phỏng lỗi sau khi DDL đã bắt đầu: service dừng khởi động, báo lỗi trạng thái nâng cấp một phần,
 *      cung cấp đường dẫn backup và hướng dẫn phục hồi; backup file tồn tại và đọc được.
 *   8. Bỏ qua logic H2 khi chạy trên CSDL khác (MySQL/Cloud).
 */
public class DemoDatabaseCheckTest {

    private Connection createTempH2Connection(Path tempDir, String dbName) throws SQLException {
        Path dbPath = tempDir.resolve(dbName);
        String url = "jdbc:h2:file:" + dbPath.toAbsolutePath().toString().replace('\\', '/') + ";DB_CLOSE_DELAY=-1";
        return DriverManager.getConnection(url, "sa", "");
    }

    private void createBaseTables(Connection conn) throws SQLException {
        try (Statement st = conn.createStatement()) {
            st.execute("CREATE TABLE topics ("
                    + "id BIGINT AUTO_INCREMENT PRIMARY KEY, "
                    + "topic_code VARCHAR(50) NOT NULL UNIQUE, "
                    + "title VARCHAR(255) NOT NULL, "
                    + "max_students INT NOT NULL"
                    + ")");

            st.execute("CREATE TABLE student_groups ("
                    + "id BIGINT AUTO_INCREMENT PRIMARY KEY, "
                    + "group_name VARCHAR(100) NOT NULL, "
                    + "member_count INT NOT NULL"
                    + ")");

            st.execute("CREATE TABLE group_members ("
                    + "id BIGINT AUTO_INCREMENT PRIMARY KEY, "
                    + "group_id BIGINT NOT NULL, "
                    + "student_id BIGINT NOT NULL, "
                    + "member_role VARCHAR(20) NOT NULL, "
                    + "CONSTRAINT fk_gm_group FOREIGN KEY (group_id) REFERENCES student_groups(id)"
                    + ")");
        }
    }

    @Test
    @DisplayName("Kịch bản 1: Preflight từ chối nhóm zero thành viên, không tự ghi giả member_count = 1, không chạy DDL")
    void testPreflightRejectsZeroMemberGroupWithoutModifyingDatabase(@TempDir Path tempDir) throws Exception {
        try (Connection conn = createTempH2Connection(tempDir, "db_zero_members")) {
            createBaseTables(conn);

            long gid = 888L;
            try (Statement st = conn.createStatement()) {
                st.executeUpdate("INSERT INTO topics(id, topic_code, title, max_students) VALUES (1, 'DT-1', 'Đề tài 1', 3)");
                // Nhóm có member_count ban đầu là 99, nhưng KHÔNG có bản ghi nào trong group_members
                st.executeUpdate("INSERT INTO student_groups(id, group_name, member_count) VALUES (" + gid + ", 'Nhóm Rỗng', 99)");
            }

            H2DemoDatabaseUpgradeService service = new H2DemoDatabaseUpgradeService(null);

            // Bắt buộc ném lỗi IllegalStateException chỉ rõ nhóm thiếu thành viên
            IllegalStateException ex = assertThrows(IllegalStateException.class, () -> service.upgradeDemoDatabase(conn));
            assertTrue(ex.getMessage().contains("Preflight thất bại"));
            assertTrue(ex.getMessage().contains("actual_count = 0"));
            assertTrue(ex.getMessage().contains(String.valueOf(gid)));
            assertTrue(ex.getMessage().contains("không ghi giả member_count = 1"));

            // Xác minh dữ liệu không bị thay đổi và member_count không bị gán giả thành 1
            try (Statement st = conn.createStatement();
                 ResultSet rs = st.executeQuery("SELECT member_count FROM student_groups WHERE id = " + gid)) {
                assertTrue(rs.next());
                assertEquals(99, rs.getInt(1), "member_count ban đầu phải được giữ nguyên, không tự sửa thành 1");
            }

            // Xác minh DDL chưa chạy (chưa tạo constraint ck_groups_size)
            try (Statement st = conn.createStatement();
                 ResultSet rs = st.executeQuery("SELECT count(*) FROM INFORMATION_SCHEMA.TABLE_CONSTRAINTS WHERE CONSTRAINT_NAME = 'CK_GROUPS_SIZE'")) {
                assertTrue(rs.next());
                assertEquals(0, rs.getInt(1), "Không được chạy DDL tạo constraint trước khi preflight hoàn tất");
            }
        }
    }

    @Test
    @DisplayName("Kịch bản 2: Preflight từ chối nhóm có > 3 thành viên, dừng nâng cấp và không chạy DDL")
    void testPreflightRejectsOverflowMemberGroupWithoutModifyingDatabase(@TempDir Path tempDir) throws Exception {
        try (Connection conn = createTempH2Connection(tempDir, "db_overflow")) {
            createBaseTables(conn);

            long gid = 777L;
            try (Statement st = conn.createStatement()) {
                st.executeUpdate("INSERT INTO topics(id, topic_code, title, max_students) VALUES (1, 'DT-1', 'Đề tài 1', 3)");
                st.executeUpdate("INSERT INTO student_groups(id, group_name, member_count) VALUES (" + gid + ", 'Nhóm Quá Tải', 3)");
                for (int i = 1; i <= 4; i++) {
                    st.executeUpdate("INSERT INTO group_members(group_id, student_id, member_role) VALUES (" + gid + ", " + i + ", 'MEMBER')");
                }
            }

            H2DemoDatabaseUpgradeService service = new H2DemoDatabaseUpgradeService(null);

            IllegalStateException ex = assertThrows(IllegalStateException.class, () -> service.upgradeDemoDatabase(conn));
            assertTrue(ex.getMessage().contains("Preflight thất bại"));
            assertTrue(ex.getMessage().contains("vượt quá quy định tối đa 3 người"));
            assertTrue(ex.getMessage().contains(String.valueOf(gid)));
            assertTrue(ex.getMessage().contains("Không tự ý cắt giảm dữ liệu thành viên"));

            // Xác minh member_count không bị cắt giảm
            try (Statement st = conn.createStatement();
                 ResultSet rs = st.executeQuery("SELECT member_count FROM student_groups WHERE id = " + gid)) {
                assertTrue(rs.next());
                assertEquals(3, rs.getInt(1));
            }
        }
    }

    @Test
    @DisplayName("Kịch bản 3: Preflight phát hiện thiếu bảng hoặc cột bắt buộc")
    void testPreflightFailsWhenRequiredTableOrColumnMissing(@TempDir Path tempDir) throws Exception {
        try (Connection conn = createTempH2Connection(tempDir, "db_missing_schema")) {
            // Chỉ tạo bảng topics, cố ý không tạo student_groups
            try (Statement st = conn.createStatement()) {
                st.execute("CREATE TABLE topics (id BIGINT PRIMARY KEY, topic_code VARCHAR(50), title VARCHAR(255), max_students INT)");
            }

            H2DemoDatabaseUpgradeService service = new H2DemoDatabaseUpgradeService(null);
            IllegalStateException ex = assertThrows(IllegalStateException.class, () -> service.upgradeDemoDatabase(conn));
            assertTrue(ex.getMessage().contains("Preflight thất bại"));
            assertTrue(ex.getMessage().contains("STUDENT_GROUPS"));
        }
    }

    @Test
    @DisplayName("Kịch bản 4: Nâng cấp thành công từ CSDL chưa có constraint, tạo backup zip hợp lệ")
    void testUpgradeFromDatabaseWithoutConstraints(@TempDir Path tempDir) throws Exception {
        try (Connection conn = createTempH2Connection(tempDir, "db_no_constraints")) {
            createBaseTables(conn);

            long t101, t102, t103;
            long g101, g102;
            try (Statement st = conn.createStatement()) {
                st.executeUpdate("INSERT INTO topics(id, topic_code, title, max_students) VALUES (101, 'DT-101', 'Đề tài nhỏ', 0)");
                st.executeUpdate("INSERT INTO topics(id, topic_code, title, max_students) VALUES (102, 'DT-102', 'Đề tài lớn', 6)");
                st.executeUpdate("INSERT INTO topics(id, topic_code, title, max_students) VALUES (103, 'DT-103', 'Đề tài chuẩn', 2)");
                t101 = 101L; t102 = 102L; t103 = 103L;

                st.executeUpdate("INSERT INTO student_groups(id, group_name, member_count) VALUES (101, 'Nhóm A', 2)");
                st.executeUpdate("INSERT INTO student_groups(id, group_name, member_count) VALUES (102, 'Nhóm B', 1)");
                g101 = 101L; g102 = 102L;

                // Nhóm 101 có 3 thành viên thực tế
                st.executeUpdate("INSERT INTO group_members(group_id, student_id, member_role) VALUES (101, 1, 'LEADER')");
                st.executeUpdate("INSERT INTO group_members(group_id, student_id, member_role) VALUES (101, 2, 'MEMBER')");
                st.executeUpdate("INSERT INTO group_members(group_id, student_id, member_role) VALUES (101, 3, 'MEMBER')");

                // Nhóm 102 có 2 thành viên thực tế
                st.executeUpdate("INSERT INTO group_members(group_id, student_id, member_role) VALUES (102, 5, 'LEADER')");
                st.executeUpdate("INSERT INTO group_members(group_id, student_id, member_role) VALUES (102, 6, 'MEMBER')");
            }

            H2DemoDatabaseUpgradeService service = new H2DemoDatabaseUpgradeService(null);
            service.upgradeDemoDatabase(conn);

            // Xác minh dữ liệu chuẩn hóa
            try (Statement st = conn.createStatement()) {
                try (ResultSet rs = st.executeQuery("SELECT max_students FROM topics WHERE id = " + t101)) {
                    assertTrue(rs.next());
                    assertEquals(1, rs.getInt(1));
                }
                try (ResultSet rs = st.executeQuery("SELECT max_students FROM topics WHERE id = " + t102)) {
                    assertTrue(rs.next());
                    assertEquals(3, rs.getInt(1));
                }
                try (ResultSet rs = st.executeQuery("SELECT max_students FROM topics WHERE id = " + t103)) {
                    assertTrue(rs.next());
                    assertEquals(2, rs.getInt(1));
                }
                try (ResultSet rs = st.executeQuery("SELECT member_count FROM student_groups WHERE id = " + g101)) {
                    assertTrue(rs.next());
                    assertEquals(3, rs.getInt(1));
                }
                try (ResultSet rs = st.executeQuery("SELECT member_count FROM student_groups WHERE id = " + g102)) {
                    assertTrue(rs.next());
                    assertEquals(2, rs.getInt(1));
                }

                // Kiểm tra ràng buộc hoạt động
                assertEquals(1, st.executeUpdate("UPDATE topics SET max_students = 2 WHERE id = " + t101));
                assertThrows(SQLException.class, () -> st.executeUpdate("UPDATE topics SET max_students = 4 WHERE id = " + t101));
                assertEquals(1, st.executeUpdate("UPDATE student_groups SET member_count = 1 WHERE id = " + g101));
                assertThrows(SQLException.class, () -> st.executeUpdate("UPDATE student_groups SET member_count = 4 WHERE id = " + g101));
            }

            // Xác minh bản sao lưu H2 backup zip được tạo trước DDL trong thư mục backups
            File backupsDir = tempDir.resolve("backups").toFile();
            assertTrue(backupsDir.exists() && backupsDir.isDirectory(), "Thư mục backups phải được tạo");
            File[] backupFiles = backupsDir.listFiles((dir, name) -> name.endsWith(".zip"));
            assertNotNull(backupFiles);
            assertTrue(backupFiles.length > 0, "Phải có ít nhất 1 tệp backup zip được tạo trước DDL");
            assertTrue(backupFiles[0].length() > 0, "Tệp backup zip phải có dung lượng > 0");
            try (ZipFile zip = new ZipFile(backupFiles[0])) {
                assertTrue(zip.size() > 0, "Tệp backup zip phải đọc được và chứa các entry");
            }
        }
    }

    @Test
    @DisplayName("Kịch bản 5: Nâng cấp thành công từ CSDL cũ CÓ RÀNG BUỘC CŨ 3–5")
    void testUpgradeFromDatabaseWithOld3To5Constraints(@TempDir Path tempDir) throws Exception {
        try (Connection conn = createTempH2Connection(tempDir, "db_old_constraints")) {
            createBaseTables(conn);

            // Gắn ràng buộc cũ của V12 (đề tài 3-5, nhóm 1-5)
            try (Statement st = conn.createStatement()) {
                st.execute("ALTER TABLE topics ADD CONSTRAINT ck_topics_size CHECK (max_students BETWEEN 3 AND 5)");
                st.execute("ALTER TABLE student_groups ADD CONSTRAINT ck_groups_size CHECK (member_count BETWEEN 1 AND 5)");

                st.executeUpdate("INSERT INTO topics(id, topic_code, title, max_students) VALUES (201, 'DT-201', 'Đề tài 1', 5)");
                st.executeUpdate("INSERT INTO topics(id, topic_code, title, max_students) VALUES (202, 'DT-202', 'Đề tài 2', 4)");
                st.executeUpdate("INSERT INTO topics(id, topic_code, title, max_students) VALUES (203, 'DT-203', 'Đề tài 3', 3)");

                st.executeUpdate("INSERT INTO student_groups(id, group_name, member_count) VALUES (201, 'Nhóm Cũ', 2)");
                st.executeUpdate("INSERT INTO group_members(group_id, student_id, member_role) VALUES (201, 11, 'LEADER')");
                st.executeUpdate("INSERT INTO group_members(group_id, student_id, member_role) VALUES (201, 12, 'MEMBER')");
                st.executeUpdate("INSERT INTO group_members(group_id, student_id, member_role) VALUES (201, 13, 'MEMBER')");
            }

            // Trước nâng cấp: CSDL cũ từ chối max_students = 2
            try (Statement st = conn.createStatement()) {
                assertThrows(SQLException.class, () ->
                        st.executeUpdate("INSERT INTO topics(topic_code, title, max_students) VALUES ('DT-ERR', 'Lỗi', 2)"));
            }

            H2DemoDatabaseUpgradeService service = new H2DemoDatabaseUpgradeService(null);
            service.upgradeDemoDatabase(conn);

            // Sau nâng cấp:
            try (Statement st = conn.createStatement()) {
                try (ResultSet rs = st.executeQuery("SELECT max_students FROM topics WHERE id = 201")) {
                    assertTrue(rs.next());
                    assertEquals(3, rs.getInt(1));
                }
                try (ResultSet rs = st.executeQuery("SELECT max_students FROM topics WHERE id = 202")) {
                    assertTrue(rs.next());
                    assertEquals(3, rs.getInt(1));
                }
                try (ResultSet rs = st.executeQuery("SELECT member_count FROM student_groups WHERE id = 201")) {
                    assertTrue(rs.next());
                    assertEquals(3, rs.getInt(1));
                }

                // Cho phép giá trị 1 và 2
                assertEquals(1, st.executeUpdate("UPDATE topics SET max_students = 1 WHERE id = 201"));
                assertEquals(1, st.executeUpdate("UPDATE student_groups SET member_count = 2 WHERE id = 201"));

                // Từ chối giá trị ngoài khoảng
                assertThrows(SQLException.class, () -> st.executeUpdate("UPDATE topics SET max_students = 4 WHERE id = 201"));
                assertThrows(SQLException.class, () -> st.executeUpdate("UPDATE student_groups SET member_count = 4 WHERE id = 201"));
            }
        }
    }

    @Test
    @DisplayName("Kịch bản 6: Tính bất biến (Idempotent) khi chạy nâng cấp nhiều lần liên tiếp")
    void testUpgradeIsIdempotent(@TempDir Path tempDir) throws Exception {
        try (Connection conn = createTempH2Connection(tempDir, "db_idempotent")) {
            createBaseTables(conn);

            try (Statement st = conn.createStatement()) {
                st.executeUpdate("INSERT INTO topics(id, topic_code, title, max_students) VALUES (301, 'DT-301', 'Đề tài 3', 3)");
                st.executeUpdate("INSERT INTO student_groups(id, group_name, member_count) VALUES (301, 'Nhóm 3', 3)");
                st.executeUpdate("INSERT INTO group_members(group_id, student_id, member_role) VALUES (301, 31, 'LEADER')");
                st.executeUpdate("INSERT INTO group_members(group_id, student_id, member_role) VALUES (301, 32, 'MEMBER')");
                st.executeUpdate("INSERT INTO group_members(group_id, student_id, member_role) VALUES (301, 33, 'MEMBER')");
            }

            H2DemoDatabaseUpgradeService service = new H2DemoDatabaseUpgradeService(null);

            // Chạy lần 1
            assertDoesNotThrow(() -> service.upgradeDemoDatabase(conn));

            // Chạy lần 2 trên database đã đạt chuẩn
            assertDoesNotThrow(() -> service.upgradeDemoDatabase(conn));

            // Xác minh dữ liệu và ràng buộc vẫn toàn vẹn
            try (Statement st = conn.createStatement()) {
                assertEquals(1, st.executeUpdate("UPDATE topics SET max_students = 1 WHERE id = 301"));
                assertThrows(SQLException.class, () -> st.executeUpdate("UPDATE topics SET max_students = 4 WHERE id = 301"));
                assertEquals(1, st.executeUpdate("UPDATE student_groups SET member_count = 2 WHERE id = 301"));
                assertThrows(SQLException.class, () -> st.executeUpdate("UPDATE student_groups SET member_count = 4 WHERE id = 301"));
            }
        }
    }

    @Test
    @DisplayName("Kịch bản 7: Mô phỏng lỗi sau khi DDL đã bắt đầu - dừng khởi động, báo trạng thái một phần, backup tồn tại/đọc được")
    void testFailureAfterDdlBeganHaltsStartupLogsPartialStateAndPreservesBackup(@TempDir Path tempDir) throws Exception {
        try (Connection conn = createTempH2Connection(tempDir, "db_post_ddl_fault")) {
            createBaseTables(conn);

            // Gắn constraint cũ để kích hoạt việc drop constraint
            try (Statement st = conn.createStatement()) {
                st.execute("ALTER TABLE topics ADD CONSTRAINT ck_topics_size CHECK (max_students BETWEEN 3 AND 5)");
                st.executeUpdate("INSERT INTO topics(id, topic_code, title, max_students) VALUES (501, 'DT-501', 'Đề tài', 3)");
                st.executeUpdate("INSERT INTO student_groups(id, group_name, member_count) VALUES (501, 'Nhóm', 2)");
                st.executeUpdate("INSERT INTO group_members(group_id, student_id, member_role) VALUES (501, 51, 'LEADER')");
                st.executeUpdate("INSERT INTO group_members(group_id, student_id, member_role) VALUES (501, 52, 'MEMBER')");
            }

            H2DemoDatabaseUpgradeService service = new H2DemoDatabaseUpgradeService(null);

            // Kích hoạt hook gây lỗi ngay sau khi DROP CONSTRAINT (khi ddlStarted = true)
            service.setPostDropHookForTesting(() -> {
                throw new RuntimeException("Mô phỏng lỗi I/O đĩa cứng đột ngột sau khi đã drop constraint cũ!");
            });

            try {
                IllegalStateException ex = assertThrows(IllegalStateException.class, () -> service.upgradeDemoDatabase(conn));

                // Bắt buộc thông báo lỗi phải chứa thông tin nâng cấp một phần
                assertTrue(ex.getMessage().contains("nâng cấp một phần"), "Lỗi phải cảnh báo database có thể ở trạng thái nâng cấp một phần");
                assertTrue(ex.getMessage().contains("Bản sao lưu trước nâng cấp tại:"), "Lỗi phải chỉ rõ đường dẫn backup");
                assertTrue(ex.getMessage().contains("hướng dẫn phục hồi"), "Lỗi phải hướng dẫn phục hồi");

                // Xác minh backup zip đã được tạo trước khi lỗi xảy ra và đọc được bằng ZipFile
                File backupsDir = tempDir.resolve("backups").toFile();
                assertTrue(backupsDir.exists());
                File[] zips = backupsDir.listFiles((d, n) -> n.endsWith(".zip"));
                assertNotNull(zips);
                assertTrue(zips.length > 0, "Bản sao lưu phải tồn tại khi lỗi xảy ra sau DDL");
                try (ZipFile zip = new ZipFile(zips[0])) {
                    assertTrue(zip.size() > 0, "Bản sao lưu phải đọc được");
                }
            } finally {
                service.setPostDropHookForTesting(null);
            }
        }
    }

    @Test
    @DisplayName("Kịch bản 8: Bỏ qua nâng cấp DDL H2 nếu CSDL không phải H2 (MySQL/Cloud)")
    void testSkipsUpgradeForNonH2Databases() throws Exception {
        Connection mockConn = Mockito.mock(Connection.class);
        DatabaseMetaData mockMeta = Mockito.mock(DatabaseMetaData.class);

        Mockito.when(mockConn.getMetaData()).thenReturn(mockMeta);
        Mockito.when(mockMeta.getDatabaseProductName()).thenReturn("MySQL");

        H2DemoDatabaseUpgradeService service = new H2DemoDatabaseUpgradeService(null);
        assertDoesNotThrow(() -> service.upgradeDemoDatabase(mockConn));

        Mockito.verify(mockConn, Mockito.never()).createStatement();
        Mockito.verify(mockConn, Mockito.never()).prepareStatement(Mockito.anyString());
    }
}
