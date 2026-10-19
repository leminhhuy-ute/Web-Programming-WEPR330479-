package topicmanagement;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.flywaydb.core.Flyway;
import org.flywaydb.core.api.output.MigrateResult;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

/**
 * Kiểm thử độc lập quy trình chuyển đổi dữ liệu legacy từ V10 sang V11:
 * - Bảo đảm an toàn tuyệt đối: chỉ cho phép Flyway.clean() trên cơ sở dữ liệu có tên 'legacy_migration_test'.
 * - Không tự động sửa đổi DB_URL, dừng với lỗi rõ ràng nếu DB_URL trỏ đến cơ sở dữ liệu khác.
 * - Không hardcode thông tin đăng nhập hoặc tự quét cổng mạng.
 * - Test MySQL chỉ chạy khi có biến môi trường LEGACY_MYSQL_MIGRATION_TEST=true.
 * - Test H2 luôn chạy độc lập trong mọi lượt build.
 */
class LegacyV10ToV11DataMigrationTest {

    private static final Pattern MYSQL_URL_PATTERN = Pattern.compile("^jdbc:mysql://[^/]+/([^?#/]+)(?:[?#].*)?$");

    @Test
    @DisplayName("H2 (MODE=MySQL): Kiểm chứng chuỗi V10 -> chèn legacy data -> V11 -> kiểm tra dữ liệu & constraint & Hibernate validate")
    void testH2LegacyV10ToV11DataMigration() throws Exception {
        String jdbcUrl = "jdbc:h2:mem:legacy_migration_test;MODE=MySQL;DB_CLOSE_DELAY=-1";
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(jdbcUrl);
        config.setUsername("sa");
        config.setPassword("");
        config.setMaximumPoolSize(5);

        try (HikariDataSource dataSource = new HikariDataSource(config)) {
            runFullV10ToV11MigrationSequence(dataSource, false, "org.hibernate.dialect.H2Dialect");
        }
    }

    @Test
    @DisplayName("MySQL 8.4 thật: Kiểm chứng chuỗi V10 -> chèn legacy data -> V11 -> kiểm tra dữ liệu & constraint & Hibernate validate")
    @EnabledIfEnvironmentVariable(named = "LEGACY_MYSQL_MIGRATION_TEST", matches = "true")
    void testRealMySql84LegacyV10ToV11DataMigration() throws Exception {
        DataSource mySqlDataSource = resolveMySqlDataSource();
        assertNotNull(mySqlDataSource, "DataSource MySQL không được null");

        try {
            runFullV10ToV11MigrationSequence(mySqlDataSource, true, "org.hibernate.dialect.MySQLDialect");
        } finally {
            if (mySqlDataSource instanceof HikariDataSource hikari) {
                hikari.close();
            }
        }
    }

    /**
     * Thực hiện tuần tự toàn bộ 8 bước kiểm tra theo yêu cầu nghiêm ngặt.
     */
    private void runFullV10ToV11MigrationSequence(DataSource dataSource, boolean isMySql, String dialect) throws Exception {
        // Kiểm tra an toàn trước khi thực hiện bất kỳ thao tác nào
        assertSafeDatabaseName(dataSource, isMySql);

        // Bước 1: Khởi tạo schema và chạy Flyway đến đúng phiên bản V10
        Flyway flywayV10 = Flyway.configure()
                .dataSource(dataSource)
                .locations("classpath:db/migration/cloud", "classpath:db/migration/mysql")
                .baselineOnMigrate(false)
                .cleanDisabled(false)
                .target("10")
                .load();

        flywayV10.clean();
        MigrateResult v10Result = flywayV10.migrate();
        assertTrue(v10Result.migrationsExecuted > 0, "Phải có ít nhất 1 migration được áp dụng để đạt V10");
        assertEquals("10", flywayV10.info().current().getVersion().toString(), "CSDL phải đang ở chính xác phiên bản V10");

        // Bước 2: Tạo dữ liệu legacy trước khi chạy V11
        try (Connection conn = dataSource.getConnection()) {
            // Chèn bộ môn mẫu để thỏa mãn foreign key department_id
            try (PreparedStatement ps = conn.prepareStatement(
                    "INSERT INTO departments(id, code, name, description) VALUES (?, ?, ?, ?)")) {
                ps.setLong(1, 999L);
                ps.setString(2, "CNPM_LEGACY");
                ps.setString(3, "Công nghệ phần mềm");
                ps.setString(4, "Bộ môn thử nghiệm chuyển đổi dữ liệu");
                ps.executeUpdate();
            }

            // Chèn người dùng có role = 'HEAD_OF_DEPT' với đầy đủ các cột thật
            try (PreparedStatement ps = conn.prepareStatement(
                    "INSERT INTO users(id, user_code, username, password_hash, full_name, email, role, department_id, status) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)")) {
                ps.setLong(1, 9991L);
                ps.setString(2, "HOD_LEGACY_01");
                ps.setString(3, "legacy_hod01");
                ps.setString(4, "$2a$10$abcdefghijklmnopqrstuu");
                ps.setString(5, "TS. Nguyễn Trưởng Bộ Môn");
                ps.setString(6, "legacy_hod01@hcmute.edu.vn");
                ps.setString(7, "HEAD_OF_DEPT");
                ps.setLong(8, 999L);
                ps.setString(9, "ACTIVE");
                assertEquals(1, ps.executeUpdate(), "Phải chèn thành công user với role HEAD_OF_DEPT tại V10");
            }

            // Chèn thông báo có audience = 'HEAD_OF_DEPT' với đầy đủ các cột thật
            try (PreparedStatement ps = conn.prepareStatement(
                    "INSERT INTO announcements(id, title, content, audience, department_id, created_by) " +
                    "VALUES (?, ?, ?, ?, ?, ?)")) {
                ps.setLong(1, 9991L);
                ps.setString(2, "Thông báo cũ dành riêng cho Trưởng bộ môn");
                ps.setString(3, "Nội dung quy trình duyệt đề tài của Trưởng bộ môn");
                ps.setString(4, "HEAD_OF_DEPT");
                ps.setLong(5, 999L);
                ps.setLong(6, 9991L);
                assertEquals(1, ps.executeUpdate(), "Phải chèn thành công announcement với audience HEAD_OF_DEPT tại V10");
            }
        }

        // Bước 3: Xác nhận hai bản ghi trên thực sự tồn tại ở trạng thái V10
        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement()) {
            try (ResultSet rs = stmt.executeQuery("SELECT count(*), role FROM users WHERE username = 'legacy_hod01' GROUP BY role")) {
                assertTrue(rs.next(), "Bản ghi legacy_hod01 phải tồn tại tại V10");
                assertEquals(1, rs.getInt(1));
                assertEquals("HEAD_OF_DEPT", rs.getString(2), "Role trước V11 phải là HEAD_OF_DEPT");
            }
            try (ResultSet rs = stmt.executeQuery("SELECT count(*), audience FROM announcements WHERE id = 9991 GROUP BY audience")) {
                assertTrue(rs.next(), "Bản ghi announcement 9991 phải tồn tại tại V10");
                assertEquals(1, rs.getInt(1));
                assertEquals("HEAD_OF_DEPT", rs.getString(2), "Audience trước V11 phải là HEAD_OF_DEPT");
            }
        }

        // Bước 4: Chạy migration V11
        Flyway flywayV11 = Flyway.configure()
                .dataSource(dataSource)
                .locations("classpath:db/migration/cloud", "classpath:db/migration/mysql")
                .baselineOnMigrate(false)
                .cleanDisabled(false)
                .target("11")
                .load();

        MigrateResult v11Result = flywayV11.migrate();
        assertEquals(1, v11Result.migrationsExecuted, "Migration V11 phải được thực thi đúng 1 lần");
        assertEquals("11", flywayV11.info().current().getVersion().toString(), "Phiên bản sau migration phải là V11");

        // Bước 5: Xác minh sau V11
        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement()) {
            // A. Người dùng cũ đã được đổi thành role = 'LECTURER'
            try (ResultSet rs = stmt.executeQuery("SELECT role FROM users WHERE username = 'legacy_hod01'")) {
                assertTrue(rs.next());
                assertEquals("LECTURER", rs.getString(1), "User legacy phải chuyển thành LECTURER sau V11");
            }

            // B. Thông báo cũ đã được đổi thành audience = 'LECTURER'
            try (ResultSet rs = stmt.executeQuery("SELECT audience FROM announcements WHERE id = 9991")) {
                assertTrue(rs.next());
                assertEquals("LECTURER", rs.getString(1), "Announcement legacy phải chuyển thành LECTURER sau V11");
            }

            // C. Không còn bản ghi users.role = 'HEAD_OF_DEPT'
            try (ResultSet rs = stmt.executeQuery("SELECT count(*) FROM users WHERE role = 'HEAD_OF_DEPT'")) {
                assertTrue(rs.next());
                assertEquals(0, rs.getInt(1), "Không được còn bất kỳ user nào có role = HEAD_OF_DEPT");
            }

            // D. Không còn bản ghi announcements.audience = 'HEAD_OF_DEPT'
            try (ResultSet rs = stmt.executeQuery("SELECT count(*) FROM announcements WHERE audience = 'HEAD_OF_DEPT'")) {
                assertTrue(rs.next());
                assertEquals(0, rs.getInt(1), "Không được còn bất kỳ announcement nào có audience = HEAD_OF_DEPT");
            }
        }

        // Bước 6: Kiểm tra ràng buộc (Constraints Enforcement)
        try (Connection conn = dataSource.getConnection()) {
            // 6.1 Constraint ck_users_role phải từ chối chèn mới role = 'HEAD_OF_DEPT'
            try (PreparedStatement ps = conn.prepareStatement(
                    "INSERT INTO users(id, user_code, username, password_hash, full_name, email, role, department_id, status) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)")) {
                ps.setLong(1, 9992L);
                ps.setString(2, "INVALID_HOD");
                ps.setString(3, "invalid_hod_attempt");
                ps.setString(4, "hash");
                ps.setString(5, "Invalid User");
                ps.setString(6, "invalid_hod@hcmute.edu.vn");
                ps.setString(7, "HEAD_OF_DEPT");
                ps.setLong(8, 999L);
                ps.setString(9, "ACTIVE");

                SQLException ex = assertThrows(SQLException.class, ps::executeUpdate,
                        "CSDL bắt buộc phải từ chối lệnh INSERT users có role = 'HEAD_OF_DEPT'");
                if (isMySql) {
                    assertEquals(3819, ex.getErrorCode(), "Mã lỗi vi phạm CHECK constraint trên MySQL phải là 3819");
                    assertTrue(ex.getMessage().contains("ck_users_role"), "Thông báo lỗi phải chỉ rõ constraint ck_users_role");
                }
            }

            // 6.2 Constraint ck_announcements_audience phải từ chối chèn mới audience = 'HEAD_OF_DEPT'
            try (PreparedStatement ps = conn.prepareStatement(
                    "INSERT INTO announcements(id, title, content, audience, department_id, created_by) " +
                    "VALUES (?, ?, ?, ?, ?, ?)")) {
                ps.setLong(1, 9992L);
                ps.setString(2, "Invalid Audience");
                ps.setString(3, "Content");
                ps.setString(4, "HEAD_OF_DEPT");
                ps.setLong(5, 999L);
                ps.setLong(6, 9991L);

                SQLException ex = assertThrows(SQLException.class, ps::executeUpdate,
                        "CSDL bắt buộc phải từ chối lệnh INSERT announcements có audience = 'HEAD_OF_DEPT'");
                if (isMySql) {
                    assertEquals(3819, ex.getErrorCode(), "Mã lỗi vi phạm CHECK constraint trên MySQL phải là 3819");
                    assertTrue(ex.getMessage().contains("ck_announcements_audience"), "Thông báo lỗi phải chỉ rõ constraint ck_announcements_audience");
                }
            }

            // 6.3 Constraint ck_users_role chấp nhận các vai trò hợp lệ: DEAN, LECTURER, STUDENT
            try (PreparedStatement ps = conn.prepareStatement(
                    "INSERT INTO users(id, user_code, username, password_hash, full_name, email, role, department_id, status) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)")) {
                ps.setLong(1, 9993L);
                ps.setString(2, "VAL_DEAN");
                ps.setString(3, "val_dean");
                ps.setString(4, "hash");
                ps.setString(5, "Dean Valid");
                ps.setString(6, "val_dean@hcmute.edu.vn");
                ps.setString(7, "DEAN");
                ps.setLong(8, 999L);
                ps.setString(9, "ACTIVE");
                assertEquals(1, ps.executeUpdate());

                ps.setLong(1, 9994L);
                ps.setString(2, "VAL_LEC");
                ps.setString(3, "val_lec");
                ps.setString(4, "hash");
                ps.setString(5, "Lecturer Valid");
                ps.setString(6, "val_lec@hcmute.edu.vn");
                ps.setString(7, "LECTURER");
                ps.setLong(8, 999L);
                ps.setString(9, "ACTIVE");
                assertEquals(1, ps.executeUpdate());

                ps.setLong(1, 9995L);
                ps.setString(2, "VAL_STU");
                ps.setString(3, "val_stu");
                ps.setString(4, "hash");
                ps.setString(5, "Student Valid");
                ps.setString(6, "val_stu@hcmute.edu.vn");
                ps.setString(7, "STUDENT");
                ps.setLong(8, 999L);
                ps.setString(9, "ACTIVE");
                assertEquals(1, ps.executeUpdate());
            }

            // 6.4 Constraint ck_announcements_audience chấp nhận các đối tượng hợp lệ: ALL, LECTURER, STUDENT
            try (PreparedStatement ps = conn.prepareStatement(
                    "INSERT INTO announcements(id, title, content, audience, department_id, created_by) " +
                    "VALUES (?, ?, ?, ?, ?, ?)")) {
                ps.setLong(1, 9993L);
                ps.setString(2, "Audience ALL");
                ps.setString(3, "Content ALL");
                ps.setString(4, "ALL");
                ps.setLong(5, 999L);
                ps.setLong(6, 9991L);
                assertEquals(1, ps.executeUpdate());

                ps.setLong(1, 9994L);
                ps.setString(2, "Audience LECTURER");
                ps.setString(3, "Content LECTURER");
                ps.setString(4, "LECTURER");
                ps.setLong(5, 999L);
                ps.setLong(6, 9991L);
                assertEquals(1, ps.executeUpdate());

                ps.setLong(1, 9995L);
                ps.setString(2, "Audience STUDENT");
                ps.setString(3, "Content STUDENT");
                ps.setString(4, "STUDENT");
                ps.setLong(5, 999L);
                ps.setLong(6, 9991L);
                assertEquals(1, ps.executeUpdate());
            }
        }

        // Bước 7: Khởi tạo Hibernate với hibernate.hbm2ddl.auto=validate và kiểm tra khớp nối Entity
        LocalContainerEntityManagerFactoryBean emf = new LocalContainerEntityManagerFactoryBean();
        emf.setDataSource(dataSource);
        emf.setPackagesToScan(
                "topicmanagement.entity",
                "topicmanagement.student",
                "topicmanagement.council",
                "topicmanagement.notification"
        );
        HibernateJpaVendorAdapter vendorAdapter = new HibernateJpaVendorAdapter();
        emf.setJpaVendorAdapter(vendorAdapter);

        Properties jpaProperties = new Properties();
        jpaProperties.put("hibernate.hbm2ddl.auto", "validate");
        jpaProperties.put("hibernate.dialect", dialect);
        jpaProperties.put("hibernate.physical_naming_strategy", "org.hibernate.boot.model.naming.CamelCaseToUnderscoresNamingStrategy");
        emf.setJpaProperties(jpaProperties);

        try {
            emf.afterPropertiesSet();
        } finally {
            emf.destroy();
        }

        // Bước 8: Kiểm tra Flyway hiện ở V11 và chạy lại không áp dụng lại migration (Idempotent)
        assertEquals("11", flywayV11.info().current().getVersion().toString(), "Phiên bản hiện tại phải là V11");
        MigrateResult reMigrateResult = flywayV11.migrate();
        assertEquals(0, reMigrateResult.migrationsExecuted, "Chạy lại migrate trên V11 phải thực hiện đúng 0 migration");
        assertEquals("11", flywayV11.info().current().getVersion().toString(), "Phiên bản sau khi chạy lại vẫn phải là V11");
    }

    /**
     * Xác thực cơ sở dữ liệu đích an toàn:
     * Tuyệt đối không cho phép Flyway.clean() nếu tên database khác 'legacy_migration_test'.
     */
    private void assertSafeDatabaseName(DataSource dataSource, boolean isMySql) throws SQLException {
        if (!isMySql) {
            return;
        }
        try (Connection conn = dataSource.getConnection()) {
            String catalog = conn.getCatalog();
            if (!"legacy_migration_test".equalsIgnoreCase(catalog)) {
                throw new IllegalStateException("Cơ sở dữ liệu đích phải có tên chính xác là 'legacy_migration_test'. Database hiện tại: " + catalog);
            }
        }
    }

    /**
     * Kết nối an toàn đến máy chủ MySQL dựa trên biến môi trường:
     * - Chỉ chạy khi LEGACY_MYSQL_MIGRATION_TEST=true.
     * - Yêu cầu DB_URL, DB_USERNAME, DB_PASSWORD rõ ràng.
     * - DB_URL phải trỏ chính xác đến database 'legacy_migration_test'.
     * - Nếu thông tin thiếu hoặc không kết nối được, test thất bại ngay lập tức (không dùng Assumption).
     */
    private DataSource resolveMySqlDataSource() throws SQLException {
        String dbUrl = System.getenv("DB_URL");
        String dbUser = System.getenv("DB_USERNAME");
        String dbPass = System.getenv("DB_PASSWORD");

        if (dbUrl == null || dbUrl.isBlank()) {
            fail("Biến môi trường DB_URL không được để trống khi LEGACY_MYSQL_MIGRATION_TEST=true");
        }
        if (dbUser == null || dbUser.isBlank()) {
            fail("Biến môi trường DB_USERNAME không được để trống khi LEGACY_MYSQL_MIGRATION_TEST=true");
        }
        if (dbPass == null) {
            fail("Biến môi trường DB_PASSWORD không được để trống khi LEGACY_MYSQL_MIGRATION_TEST=true");
        }

        String trimmedUrl = dbUrl.trim();
        Matcher matcher = MYSQL_URL_PATTERN.matcher(trimmedUrl);
        if (!matcher.matches() || !"legacy_migration_test".equalsIgnoreCase(matcher.group(1))) {
            fail("DB_URL phải trỏ chính xác tới cơ sở dữ liệu 'legacy_migration_test'. URL hiện tại: " + dbUrl);
        }

        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(trimmedUrl);
        config.setUsername(dbUser);
        config.setPassword(dbPass);
        config.setDriverClassName("com.mysql.cj.jdbc.Driver");
        config.setMaximumPoolSize(5);
        config.setMinimumIdle(1);
        config.setConnectionTimeout(5000);

        HikariDataSource ds = new HikariDataSource(config);
        try (Connection conn = ds.getConnection()) {
            String catalog = conn.getCatalog();
            if (!"legacy_migration_test".equalsIgnoreCase(catalog)) {
                ds.close();
                fail("Catalog của kết nối MySQL không khớp 'legacy_migration_test', thực tế là: " + catalog);
            }
        } catch (SQLException e) {
            ds.close();
            fail("Không thể kết nối tới MySQL với cấu hình được cung cấp: " + e.getMessage());
        }

        return ds;
    }
}
