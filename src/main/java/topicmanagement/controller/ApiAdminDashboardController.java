package topicmanagement.controller;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import topicmanagement.council.CouncilRepository;
import topicmanagement.council.DefenseRepository;
import topicmanagement.dto.ApiResponse;
import topicmanagement.dto.response.AdminDashboardResponse;
import topicmanagement.enums.GroupStatus;
import topicmanagement.enums.TopicStatus;
import topicmanagement.repository.RegistrationPeriodRepository;
import topicmanagement.repository.StudentGroupRepository;
import topicmanagement.repository.TopicRepository;
import topicmanagement.repository.UserRepository;

@org.springframework.security.access.prepost.PreAuthorize("hasRole('DEAN')")
@RestController
@RequestMapping("/api/admin/dashboard")
public class ApiAdminDashboardController {
  private final UserRepository users;
  private final RegistrationPeriodRepository periods;
  private final TopicRepository topics;
  private final StudentGroupRepository groups;
  private final CouncilRepository councils;
  private final DefenseRepository defenses;

  public ApiAdminDashboardController(UserRepository users, RegistrationPeriodRepository periods,
      TopicRepository topics, StudentGroupRepository groups, CouncilRepository councils,
      DefenseRepository defenses) {
    this.users = users;
    this.periods = periods;
    this.topics = topics;
    this.groups = groups;
    this.councils = councils;
    this.defenses = defenses;
  }

  @GetMapping
  public ApiResponse<AdminDashboardResponse> statistics() {
    LocalDateTime now = LocalDateTime.now();
    return ApiResponse.ok(new AdminDashboardResponse(users.count(), periods.countActiveAt(now),
        topics.countByStatus(TopicStatus.PENDING), topics.countByStatus(TopicStatus.APPROVED),
        groups.countByStatus(GroupStatus.APPROVED), councils.count(), defenses.countByPublishedTrue()));
  }
}
