package topicmanagement.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import topicmanagement.dto.ApiResponse;
import topicmanagement.dto.response.ProfileResponse;
import topicmanagement.service.ProfileService;

@RestController
@RequestMapping("/api/profile")
public class ProfileController {
    private final ProfileService profiles;

    public ProfileController(ProfileService profiles) {
        this.profiles = profiles;
    }

    public record PasswordRequest(@NotBlank String currentPassword,
                                  @NotBlank String newPassword,
                                  @NotBlank String confirmPassword) {}

    @GetMapping
    public ApiResponse<ProfileResponse> get() {
        return ApiResponse.ok(profiles.profile());
    }

    @PostMapping("/password")
    public ApiResponse<Void> changePassword(@Valid @RequestBody PasswordRequest request,
            HttpServletRequest http) {
        profiles.changePassword(request.currentPassword(), request.newPassword(), request.confirmPassword());
        var session = http.getSession(false);
        if (session != null) session.invalidate();
        SecurityContextHolder.clearContext();
        return ApiResponse.ok("Đã đổi mật khẩu. Vui lòng đăng nhập lại.", null);
    }
}
