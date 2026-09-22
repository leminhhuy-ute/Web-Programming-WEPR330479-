package topicmanagement.service;

import java.util.List;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import topicmanagement.dto.response.UserResponse;
import topicmanagement.entity.User;
import topicmanagement.enums.Role;
import topicmanagement.repository.UserRepository;

@Service
public class UserService {
    private final UserRepository userRepository;

    @Autowired
    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Transactional(readOnly = true)
    public List<UserResponse> getLecturersByDepartment(Long departmentId) {
        List<User> lecturers;
        if (departmentId != null) {
            lecturers = userRepository.findByDepartmentIdAndRoleIn(departmentId, List.of(Role.LECTURER, Role.HEAD_OF_DEPT));
        } else {
            lecturers = userRepository.findByRole(Role.LECTURER);
            lecturers.addAll(userRepository.findByRole(Role.HEAD_OF_DEPT));
        }
        return lecturers.stream().map(UserResponse::new).collect(Collectors.toList());
    }
}
