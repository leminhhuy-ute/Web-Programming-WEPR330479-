package vn.edu.hcmute.topicmanagement;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import vn.edu.hcmute.topicmanagement.dto.request.AnnouncementRequest;
import vn.edu.hcmute.topicmanagement.dto.request.DepartmentRequest;
import vn.edu.hcmute.topicmanagement.dto.request.RegistrationPeriodRequest;
import vn.edu.hcmute.topicmanagement.dto.request.UserRequest;
import vn.edu.hcmute.topicmanagement.dto.response.DepartmentResponse;
import vn.edu.hcmute.topicmanagement.dto.response.RegistrationPeriodResponse;
import vn.edu.hcmute.topicmanagement.dto.response.UserResponse;
import vn.edu.hcmute.topicmanagement.enums.AnnouncementAudience;
import vn.edu.hcmute.topicmanagement.enums.RegistrationPeriodType;
import vn.edu.hcmute.topicmanagement.enums.Role;
import vn.edu.hcmute.topicmanagement.enums.UserStatus;
import vn.edu.hcmute.topicmanagement.exception.ConflictException;
import vn.edu.hcmute.topicmanagement.service.AnnouncementService;
import vn.edu.hcmute.topicmanagement.service.DepartmentService;
import vn.edu.hcmute.topicmanagement.service.RegistrationPeriodService;
import vn.edu.hcmute.topicmanagement.service.UserService;

import java.time.LocalDate;
import java.time.LocalDateTime;

@SpringBootTest
@Transactional
class AdminModuleIntegrationTest {

    @Autowired
    private UserService userService;

    @Autowired
    private DepartmentService departmentService;

    @Autowired
    private RegistrationPeriodService periodService;

    @Autowired
    private AnnouncementService announcementService;

    @Test
    void testDepartmentLifecycle() {
        DepartmentRequest req = new DepartmentRequest("TEST_DEPT", "Bộ môn Kiểm thử Phần mềm", "Bộ môn thử nghiệm");
        DepartmentResponse dept = departmentService.create(req);
        Assertions.assertNotNull(dept.id());
        Assertions.assertEquals("TEST_DEPT", dept.code());

        // Duplicate code check
        Assertions.assertThrows(ConflictException.class, () -> departmentService.create(req));
    }

    @Test
    void testUserManagementAndDeanConstraint() {
        UserRequest userReq = new UserRequest(
                "TEST_GV_99",
                "test_gv_99",
                "Password@123",
                "Giảng viên Kiểm thử",
                "testgv99@hcmute.edu.vn",
                Role.LECTURER,
                null,
                UserStatus.ACTIVE
        );
        UserResponse created = userService.create(userReq);
        Assertions.assertNotNull(created.id());
        Assertions.assertEquals("TEST_GV_99", created.userCode());

        // Cannot delete self or last dean
        UserResponse dean = userService.findAll("dean", Role.DEAN, null).get(0);
        Assertions.assertThrows(IllegalArgumentException.class, () -> userService.delete(dean.id(), dean.id()));
    }

    @Test
    void testPeriodDateValidation() {
        LocalDateTime now = LocalDateTime.now();
        RegistrationPeriodRequest invalidOrder = new RegistrationPeriodRequest(
                "Đợt kiểm thử lỗi ngày",
                RegistrationPeriodType.KLTN,
                now.plusDays(10), // start
                now.plusDays(5),  // end before start
                now.plusDays(15),
                now.plusDays(25),
                now.plusDays(35),
                LocalDate.now().plusDays(40)
        );
        Assertions.assertThrows(IllegalArgumentException.class, () -> periodService.create(invalidOrder));
    }

    @Test
    void testAnnouncementBroadcast() {
        UserResponse dean = userService.findAll("dean", Role.DEAN, null).get(0);
        AnnouncementRequest req = new AnnouncementRequest(
                "Thông báo kiểm thử tự động",
                "Nội dung kiểm thử thông báo toàn khoa",
                AnnouncementAudience.ALL
        );
        var created = announcementService.create(req, dean.id());
        Assertions.assertNotNull(created.id());
        Assertions.assertEquals("Thông báo kiểm thử tự động", created.title());
    }
}
