package topicmanagement.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import topicmanagement.service.TopicPolicy;

import javax.sql.DataSource;
import java.io.File;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Dịch vụ nâng cấp an toàn cấu trúc và dữ liệu cho cơ sở dữ liệu demo H2.
 * Chỉ áp dụng cho profile "demo" khi kết nối tới H2 database.
 * Tuyệt đối không chạy trên MySQL / Cloud (được quản lý bởi Flyway V13).
 *
 * Lưu ý kỹ thuật quan trọng về H2 DDL:
 * Trong H2, các câu lệnh DDL như ALTER TABLE ... DROP/ADD CONSTRAINT tự động commit và
 * KHÔNG THỂ rollback qua giao dịch (conn.rollback() không thể hoàn tác DDL).
 * Do đó, kiến trúc bảo vệ gồm:
 * 1. Preflight kiểm tra toàn diện cấu trúc và dữ liệu trước khi thực hiện bất kỳ DDL nào.
 * 2. Tạo bản sao lưu nhất quán bằng BACKUP TO của H2 (cho file-based database) trước DDL.
 * 3. Nếu xảy ra sự cố sau khi DDL đã bắt đầu, dừng khởi động ngay (fail-fast), ghi nhận
 *    cơ sở dữ liệu ở trạng thái nâng cấp một phần, thông báo đường dẫn backup và hướng dẫn phục hồi.
 */
@Service
@Profile("demo")
public class H2DemoDatabaseUpgradeService {

    private static final int TOPIC_MIN = TopicPolicy.MIN_TOPIC_STUDENTS;
    private static final int TOPIC_MAX = TopicPolicy.MAX_TOPIC_STUDENTS;
    private static final int GROUP_MIN = TopicPolicy.MIN_GROUP_SIZE;
    private static final int GROUP_MAX = TopicPolicy.MAX_GROUP_SIZE;
    private static final String TOPIC_CHECK = "BETWEEN " + TOPIC_MIN + " AND " + TOPIC_MAX;
    private static final String GROUP_CHECK = "BETWEEN " + GROUP_MIN + " AND " + GROUP_MAX;

    private static final Logger log = LoggerFactory.getLogger(H2DemoDatabaseUpgradeService.class);

    private final DataSource dataSource;

    // Test hook hỗ trợ mô phỏng lỗi ngay sau khi DDL đã bắt đầu
    private Runnable postDropHookForTesting;

    public H2DemoDatabaseUpgradeService(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public void setPostDropHookForTesting(Runnable hook) {
        this.postDropHookForTesting = hook;
    }

    public void upgradeIfApplicable() {
        if (dataSource != null) {
            upgradeDemoDatabase(dataSource);
        }
    }

    public void upgradeDemoDatabase(DataSource ds) {
        try (Connection conn = ds.getConnection()) {
            upgradeDemoDatabase(conn);
        } catch (SQLException e) {
            log.error("[H2DemoUpgrade] Lỗi kết nối CSDL demo để nâng cấp: {}", e.getMessage(), e);
            throw new IllegalStateException("Không thể kết nối CSDL demo H2 để nâng cấp: " + e.getMessage(), e);
        }
    }

    public void upgradeDemoDatabase(Connection conn) throws SQLException {
        String dbProduct = conn.getMetaData().getDatabaseProductName();
        if (!"H2".equalsIgnoreCase(dbProduct)) {
            log.info("[H2DemoUpgrade] Bỏ qua nâng cấp schema thủ công vì cơ sở dữ liệu không phải H2 (hiện tại: {}). Flyway sẽ quản lý schema.", dbProduct);
            return;
        }

        log.info("[H2DemoUpgrade] Bắt đầu bước preflight kiểm tra schema và dữ liệu demo H2...");

        // BƯỚC 1: PREFLIGHT CHECKS (Hoàn toàn chỉ đọc, phát hiện lỗi trước khi can thiệp CSDL)
        PreflightAssessment assessment = performPreflightAssessment(conn);

        // Kiểm tra xem có cần thực hiện nâng cấp DDL/DML hay không
        if (!assessment.isUpgradeNeeded()) {
            log.info("[H2DemoUpgrade] Cơ sở dữ liệu demo H2 đã đạt chuẩn quy mô V13 (constraints và dữ liệu đều hợp lệ). Không cần chạy nâng cấp.");
            return;
        }

        log.info("[H2DemoUpgrade] Phát hiện schema/dữ liệu cần nâng cấp: {} constraint cũ cần drop, {} nhóm cần sync, topics cần chuẩn hóa: {}, constraints hợp lệ hiện diện: [topics={}, groups={}].",
                assessment.invalidConstraintsToDrop.size(),
                assessment.validGroupsToSync.size(),
                assessment.topicDataNeedsNormalization,
                assessment.hasValidTopicConstraint,
                assessment.hasValidGroupConstraint);

        // BƯỚC 2: TẠO BẢN SAO LƯU NHẤT QUÁN TRƯỚC DDL (Áp dụng cho H2 dạng file)
        String backupPath = null;
        if (isFileBasedH2(conn)) {
            backupPath = createConsistentH2Backup(conn);
        }

        // BƯỚC 3: THỰC THI DDL VÀ DML VỚI CƠ CHẾ FAIL-FAST & HƯỚNG DẪN PHỤC HỒI
        boolean ddlStarted = false;
        try {
            ddlStarted = true;

            // 3.1. Gỡ bỏ các ràng buộc cũ/sai
            for (ConstraintTarget ct : assessment.invalidConstraintsToDrop) {
                log.warn("[H2DemoUpgrade] Gỡ bỏ ràng buộc cũ không hợp lệ trên bảng {} ({}).", ct.tableName, ct.constraintName);
                try (Statement st = conn.createStatement()) {
                    st.execute("ALTER TABLE " + ct.tableName + " DROP CONSTRAINT " + ct.constraintName);
                }
            }

            // Kích hoạt test hook nếu có để kiểm thử kịch bản lỗi sau DDL
            if (postDropHookForTesting != null) {
                postDropHookForTesting.run();
            }

            // 3.2. Chuẩn hóa dữ liệu topics.max_students về khoảng [TOPIC_MIN, TOPIC_MAX]
            if (assessment.topicDataNeedsNormalization) {
                try (Statement st = conn.createStatement()) {
                    int lowCount = st.executeUpdate("UPDATE topics SET max_students = " + TOPIC_MIN + " WHERE max_students < " + TOPIC_MIN);
                    int highCount = st.executeUpdate("UPDATE topics SET max_students = " + TOPIC_MAX + " WHERE max_students > " + TOPIC_MAX);
                    log.info("[H2DemoUpgrade] Đã chuẩn hóa đề tài: {} đề tài nâng lên {}, {} đề tài hạ xuống {}.", lowCount, TOPIC_MIN, highCount, TOPIC_MAX);
                }
            }

            // 3.3. Đồng bộ student_groups.member_count bằng đúng actual_count thực tế (từ GROUP_MIN đến GROUP_MAX)
            if (!assessment.validGroupsToSync.isEmpty()) {
                try (PreparedStatement ps = conn.prepareStatement("UPDATE student_groups SET member_count = ? WHERE id = ?")) {
                    for (Map.Entry<Long, Integer> entry : assessment.validGroupsToSync.entrySet()) {
                        long groupId = entry.getKey();
                        int actualCount = entry.getValue();
                        ps.setInt(1, actualCount);
                        ps.setLong(2, groupId);
                        ps.addBatch();
                    }
                    ps.executeBatch();
                }
                log.info("[H2DemoUpgrade] Đã đồng bộ sĩ số member_count cho {} nhóm sinh viên.", assessment.validGroupsToSync.size());
            }

            // 3.4. Đảm bảo ràng buộc ck_topics_size (TOPIC_CHECK) được tạo
            if (!hasValidTopicConstraint(conn)) {
                try (Statement st = conn.createStatement()) {
                    st.execute("ALTER TABLE topics ADD CONSTRAINT ck_topics_size CHECK (max_students " + TOPIC_CHECK + ")");
                    log.info("[H2DemoUpgrade] Đã tạo mới thành công ràng buộc ck_topics_size (max_students {}).", TOPIC_CHECK);
                }
            }

            // 3.5. Đảm bảo ràng buộc ck_groups_size (GROUP_CHECK) được tạo
            if (!hasValidGroupConstraint(conn)) {
                try (Statement st = conn.createStatement()) {
                    st.execute("ALTER TABLE student_groups ADD CONSTRAINT ck_groups_size CHECK (member_count " + GROUP_CHECK + ")");
                    log.info("[H2DemoUpgrade] Đã tạo mới thành công ràng buộc ck_groups_size (member_count {}).", GROUP_CHECK);
                }
            }

            // 3.6. Kiểm tra hậu điều kiện (Post-verification)
            if (!hasValidTopicConstraint(conn)) {
                throw new IllegalStateException("Hậu kiểm thất bại: không tìm thấy ràng buộc ck_topics_size (" + TOPIC_MIN + ".." + TOPIC_MAX + ") trên bảng topics sau khi nâng cấp.");
            }
            if (!hasValidGroupConstraint(conn)) {
                throw new IllegalStateException("Hậu kiểm thất bại: không tìm thấy ràng buộc ck_groups_size (" + GROUP_MIN + ".." + GROUP_MAX + ") trên bảng student_groups sau khi nâng cấp.");
            }

            log.info("[H2DemoUpgrade] Nâng cấp schema và đồng bộ dữ liệu database demo H2 hoàn tất thành công.");
        } catch (Exception e) {
            if (!ddlStarted) {
                log.error("[H2DemoUpgrade] Nâng cấp thất bại trước khi DDL bắt đầu. CSDL chưa bị thay đổi: {}", e.getMessage(), e);
                if (e instanceof RuntimeException re) throw re;
                throw new IllegalStateException("Preflight thất bại: " + e.getMessage(), e);
            } else {
                log.error("[H2DemoUpgrade] CẢNH BÁO NGUY HIỂM: Nâng cấp schema H2 thất bại sau khi DDL đã bắt đầu!\n"
                        + "Lưu ý kỹ thuật: Trong cơ sở dữ liệu H2, các lệnh DDL (ALTER TABLE DROP/ADD CONSTRAINT) tự động commit và KHÔNG THỂ rollback qua giao dịch.\n"
                        + "Cơ sở dữ liệu có thể đang ở trạng thái nâng cấp một phần (partially upgraded).\n"
                        + "Bản sao lưu trước nâng cấp: {}\n"
                        + "HƯỚNG DẪN PHỤC HỒI:\n"
                        + "1. Dừng ngay ứng dụng.\n"
                        + "2. Sao lưu/di chuyển file .mv.db hiện tại ra thư mục khác để lưu vết sự cố.\n"
                        + "3. Giải nén bản sao lưu từ '{}' để khôi phục file .mv.db nguyên bản trước nâng cấp.\n"
                        + "4. Khắc phục nguyên nhân lỗi trước khi khởi động lại.",
                        (backupPath != null ? backupPath : "N/A"),
                        (backupPath != null ? backupPath : "N/A"), e);

                String errMsg = "Nâng cấp database demo H2 thất bại sau khi DDL đã bắt đầu (cơ sở dữ liệu có thể ở trạng thái nâng cấp một phần). "
                        + "Bản sao lưu trước nâng cấp tại: " + (backupPath != null ? backupPath : "N/A") + ". "
                        + "Chi tiết lỗi: " + e.getMessage() + ". "
                        + "Vui lòng xem hướng dẫn phục hồi trong log và khôi phục từ bản sao lưu.";
                throw new IllegalStateException(errMsg, e);
            }
        }
    }

    private PreflightAssessment performPreflightAssessment(Connection conn) throws SQLException {
        PreflightAssessment assessment = new PreflightAssessment();

        // 1. Kiểm tra các bảng và cột bắt buộc tồn tại
        validateRequiredTablesAndColumns(conn);

        // 2. Kiểm tra dữ liệu nhóm và thành viên
        validateGroupAndMemberCounts(conn, assessment);

        // 3. Kiểm tra dữ liệu đề tài
        validateTopicData(conn, assessment);

        // 4. Xác định các ràng buộc hiện có và các ràng buộc cũ cần thay thế
        inspectConstraints(conn, assessment);

        return assessment;
    }

    private void validateRequiredTablesAndColumns(Connection conn) throws SQLException {
        if (!tableExists(conn, "STUDENT_GROUPS")) {
            throw new IllegalStateException("Preflight thất bại: Không tìm thấy bảng bắt buộc STUDENT_GROUPS.");
        }
        if (!columnExists(conn, "STUDENT_GROUPS", "MEMBER_COUNT")) {
            throw new IllegalStateException("Preflight thất bại: Bảng STUDENT_GROUPS thiếu cột bắt buộc MEMBER_COUNT.");
        }
        if (!tableExists(conn, "TOPICS")) {
            throw new IllegalStateException("Preflight thất bại: Không tìm thấy bảng bắt buộc TOPICS.");
        }
        if (!columnExists(conn, "TOPICS", "MAX_STUDENTS")) {
            throw new IllegalStateException("Preflight thất bại: Bảng TOPICS thiếu cột bắt buộc MAX_STUDENTS.");
        }
        if (!tableExists(conn, "GROUP_MEMBERS")) {
            throw new IllegalStateException("Preflight thất bại: Không tìm thấy bảng bắt buộc GROUP_MEMBERS.");
        }
        if (!columnExists(conn, "GROUP_MEMBERS", "GROUP_ID")) {
            throw new IllegalStateException("Preflight thất bại: Bảng GROUP_MEMBERS thiếu cột bắt buộc GROUP_ID.");
        }
    }

    private void validateGroupAndMemberCounts(Connection conn, PreflightAssessment assessment) throws SQLException {
        String sql = "SELECT g.id, g.member_count, count(gm.id) AS actual_count "
                   + "FROM student_groups g "
                   + "LEFT JOIN group_members gm ON g.id = gm.group_id "
                   + "GROUP BY g.id, g.member_count";

        List<Long> overflowGroupIds = new ArrayList<>();
        List<Long> zeroMemberGroupIds = new ArrayList<>();

        try (Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                long groupId = rs.getLong("id");
                int currentMemberCount = rs.getInt("member_count");
                int actualCount = rs.getInt("actual_count");

                if (actualCount > GROUP_MAX) {
                    overflowGroupIds.add(groupId);
                } else if (actualCount == 0) {
                    zeroMemberGroupIds.add(groupId);
                } else {
                    // actualCount từ GROUP_MIN đến GROUP_MAX: hợp lệ
                    if (currentMemberCount != actualCount) {
                        assessment.validGroupsToSync.put(groupId, actualCount);
                    }
                }
            }
        }

        // Báo lỗi nếu có nhóm vượt quá GROUP_MAX thành viên (không tự ý cắt giảm thành viên)
        if (!overflowGroupIds.isEmpty()) {
            throw new IllegalStateException("Preflight thất bại: Phát hiện các nhóm ID " + overflowGroupIds
                    + " có số thành viên thực tế trong group_members vượt quá quy định tối đa " + GROUP_MAX + " người. "
                    + "Không tự ý cắt giảm dữ liệu thành viên; dừng nâng cấp để xử lý thủ công "
                    + "(hoặc xóa thư mục data/ để tạo lại dữ liệu demo).");
        }

        // Báo lỗi nếu có nhóm không có thành viên nào (actual_count = 0)
        // Không tự tạo thành viên, không suy đoán nhóm trưởng và không ghi giả member_count = 1
        if (!zeroMemberGroupIds.isEmpty()) {
            throw new IllegalStateException("Preflight thất bại: Phát hiện các nhóm ID " + zeroMemberGroupIds
                    + " không có bản ghi nào trong group_members (actual_count = 0). "
                    + "Dừng nâng cấp vì thiếu dữ liệu thành viên; không tự tạo thành viên, không suy đoán nhóm trưởng và không ghi giả member_count = 1.");
        }
    }

    private void validateTopicData(Connection conn, PreflightAssessment assessment) throws SQLException {
        String sql = "SELECT count(*) FROM topics WHERE max_students < " + TOPIC_MIN + " OR max_students > " + TOPIC_MAX;
        try (Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            if (rs.next() && rs.getInt(1) > 0) {
                assessment.topicDataNeedsNormalization = true;
            }
        }
    }

    private void inspectConstraints(Connection conn, PreflightAssessment assessment) throws SQLException {
        // Kiểm tra constraints trên TOPICS
        for (ConstraintInfo ci : getCheckConstraints(conn, "TOPICS")) {
            String exprUpper = ci.clause != null ? ci.clause.toUpperCase(Locale.ROOT) : "";
            if (exprUpper.contains("MAX_STUDENTS") || ci.name.equalsIgnoreCase("CK_TOPICS_SIZE")) {
                if (matchesRange(exprUpper, TOPIC_MIN, TOPIC_MAX)) {
                    assessment.hasValidTopicConstraint = true;
                } else {
                    assessment.invalidConstraintsToDrop.add(new ConstraintTarget("TOPICS", ci.name));
                }
            }
        }

        // Kiểm tra constraints trên STUDENT_GROUPS
        for (ConstraintInfo ci : getCheckConstraints(conn, "STUDENT_GROUPS")) {
            String exprUpper = ci.clause != null ? ci.clause.toUpperCase(Locale.ROOT) : "";
            if (exprUpper.contains("MEMBER_COUNT") || ci.name.equalsIgnoreCase("CK_GROUPS_SIZE")) {
                if (matchesRange(exprUpper, GROUP_MIN, GROUP_MAX)) {
                    assessment.hasValidGroupConstraint = true;
                } else {
                    assessment.invalidConstraintsToDrop.add(new ConstraintTarget("STUDENT_GROUPS", ci.name));
                }
            }
        }
    }

    private boolean isFileBasedH2(Connection conn) throws SQLException {
        String url = conn.getMetaData().getURL();
        return url != null && url.startsWith("jdbc:h2:file:");
    }

    private String createConsistentH2Backup(Connection conn) throws SQLException {
        String url = conn.getMetaData().getURL();
        String pathPart = url.substring("jdbc:h2:file:".length());
        int semi = pathPart.indexOf(';');
        if (semi >= 0) {
            pathPart = pathPart.substring(0, semi);
        }
        File dbBase = new File(pathPart);
        File parentDir = dbBase.getParentFile();
        if (parentDir == null) {
            parentDir = new File(".");
        }
        File backupsDir = new File(parentDir, "backups");
        if (!backupsDir.exists()) {
            backupsDir.mkdirs();
        }

        String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss_SSS").format(new java.util.Date());
        File backupFile = new File(backupsDir, dbBase.getName() + "_pre_upgrade_" + timestamp + ".zip");
        String backupPath = backupFile.getAbsolutePath().replace('\\', '/');

        log.info("[H2DemoUpgrade] Đang tạo bản sao lưu nhất quán của CSDL H2 trước DDL tại: {}", backupPath);
        try (Statement st = conn.createStatement()) {
            st.execute("BACKUP TO '" + backupPath.replace("'", "''") + "'");
        }

        if (!backupFile.exists() || backupFile.length() == 0) {
            throw new IllegalStateException("Tạo bản sao lưu H2 thất bại: tệp sao lưu không tồn tại hoặc rỗng (" + backupPath + ")");
        }
        log.info("[H2DemoUpgrade] Đã tạo thành công bản sao lưu H2 ({} bytes).", backupFile.length());
        return backupPath;
    }

    private boolean tableExists(Connection conn, String tableName) throws SQLException {
        String sql = "SELECT count(*) FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_SCHEMA = 'PUBLIC' AND UPPER(TABLE_NAME) = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, tableName.toUpperCase(Locale.ROOT));
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() && rs.getInt(1) > 0;
            }
        }
    }

    private boolean columnExists(Connection conn, String tableName, String columnName) throws SQLException {
        String sql = "SELECT count(*) FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA = 'PUBLIC' AND UPPER(TABLE_NAME) = ? AND UPPER(COLUMN_NAME) = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, tableName.toUpperCase(Locale.ROOT));
            ps.setString(2, columnName.toUpperCase(Locale.ROOT));
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() && rs.getInt(1) > 0;
            }
        }
    }

    private List<ConstraintInfo> getCheckConstraints(Connection conn, String tableName) throws SQLException {
        List<ConstraintInfo> result = new ArrayList<>();
        String sql = "SELECT tc.CONSTRAINT_NAME, cc.CHECK_CLAUSE "
                   + "FROM INFORMATION_SCHEMA.TABLE_CONSTRAINTS tc "
                   + "JOIN INFORMATION_SCHEMA.CHECK_CONSTRAINTS cc "
                   + "  ON tc.CONSTRAINT_NAME = cc.CONSTRAINT_NAME AND tc.CONSTRAINT_SCHEMA = cc.CONSTRAINT_SCHEMA "
                   + "WHERE tc.TABLE_SCHEMA = 'PUBLIC' AND tc.TABLE_NAME = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, tableName.toUpperCase(Locale.ROOT));
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    result.add(new ConstraintInfo(rs.getString("CONSTRAINT_NAME"), rs.getString("CHECK_CLAUSE")));
                }
            }
        }
        return result;
    }

    /** True when a CHECK clause expresses exactly the [min, max] range (BETWEEN or >= / <= form). */
    private static boolean matchesRange(String exprUpper, int min, int max) {
        return exprUpper.contains("BETWEEN " + min + " AND " + max)
            || (exprUpper.contains(">= " + min) && exprUpper.contains("<= " + max));
    }

    private boolean hasValidTopicConstraint(Connection conn) throws SQLException {
        for (ConstraintInfo ci : getCheckConstraints(conn, "TOPICS")) {
            String exprUpper = ci.clause != null ? ci.clause.toUpperCase(Locale.ROOT) : "";
            if (exprUpper.contains("MAX_STUDENTS") && matchesRange(exprUpper, TOPIC_MIN, TOPIC_MAX)) {
                return true;
            }
        }
        return false;
    }

    private boolean hasValidGroupConstraint(Connection conn) throws SQLException {
        for (ConstraintInfo ci : getCheckConstraints(conn, "STUDENT_GROUPS")) {
            String exprUpper = ci.clause != null ? ci.clause.toUpperCase(Locale.ROOT) : "";
            if (exprUpper.contains("MEMBER_COUNT") && matchesRange(exprUpper, GROUP_MIN, GROUP_MAX)) {
                return true;
            }
        }
        return false;
    }

    public static class ConstraintInfo {
        public final String name;
        public final String clause;

        public ConstraintInfo(String name, String clause) {
            this.name = name;
            this.clause = clause;
        }
    }

    public static class ConstraintTarget {
        public final String tableName;
        public final String constraintName;

        public ConstraintTarget(String tableName, String constraintName) {
            this.tableName = tableName;
            this.constraintName = constraintName;
        }
    }

    public static class PreflightAssessment {
        public final List<ConstraintTarget> invalidConstraintsToDrop = new ArrayList<>();
        public final Map<Long, Integer> validGroupsToSync = new LinkedHashMap<>();
        public boolean topicDataNeedsNormalization = false;
        public boolean hasValidTopicConstraint = false;
        public boolean hasValidGroupConstraint = false;

        public boolean isUpgradeNeeded() {
            return !invalidConstraintsToDrop.isEmpty()
                    || !validGroupsToSync.isEmpty()
                    || topicDataNeedsNormalization
                    || !hasValidTopicConstraint
                    || !hasValidGroupConstraint;
        }
    }
}
