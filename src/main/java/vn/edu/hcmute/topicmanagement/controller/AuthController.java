package vn.edu.hcmute.topicmanagement.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import vn.edu.hcmute.topicmanagement.security.CustomUserDetails;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
public class AuthController {

    @GetMapping("/login")
    public String loginPage(@RequestParam(value = "error", required = false) String error,
                            @RequestParam(value = "logout", required = false) String logout,
                            Authentication authentication,
                            Model model) {
        if (authentication != null && authentication.isAuthenticated() &&
                !(authentication.getPrincipal() instanceof String && "anonymousUser".equals(authentication.getPrincipal()))) {
            return redirectToUserDashboard(authentication);
        }

        if (error != null) {
            model.addAttribute("errorMessage", "Tên đăng nhập / MSSV hoặc mật khẩu không chính xác!");
        }
        if (logout != null) {
            model.addAttribute("successMessage", "Đã đăng xuất thành công khỏi hệ thống.");
        }
        return "login";
    }

    @GetMapping("/")
    public String home(Authentication authentication) {
        if (authentication != null && authentication.isAuthenticated() &&
                !(authentication.getPrincipal() instanceof String && "anonymousUser".equals(authentication.getPrincipal()))) {
            return redirectToUserDashboard(authentication);
        }
        return "redirect:/login";
    }

    @GetMapping("/api/auth/me")
    @ResponseBody
    public ResponseEntity<?> getCurrentUser(@AuthenticationPrincipal CustomUserDetails userDetails) {
        if (userDetails == null) {
            return ResponseEntity.status(401).body(Map.of("authenticated", false));
        }

        Map<String, Object> data = new HashMap<>();
        data.put("authenticated", true);
        data.put("id", userDetails.getId());
        data.put("userCode", userDetails.getUserCode());
        data.put("username", userDetails.getUsername());
        data.put("fullName", userDetails.getFullName());
        data.put("email", userDetails.getEmail());
        data.put("role", userDetails.getRole().name());
        if (userDetails.getDepartment() != null) {
            data.put("departmentId", userDetails.getDepartment().getId());
            data.put("departmentName", userDetails.getDepartment().getName());
        }
        data.put("authorities", userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList()));

        return ResponseEntity.ok(data);
    }

    private String redirectToUserDashboard(Authentication authentication) {
        for (GrantedAuthority auth : authentication.getAuthorities()) {
            String role = auth.getAuthority();
            if ("ROLE_DEAN".equals(role)) {
                return "redirect:/admin/dashboard";
            } else if ("ROLE_HEAD_OF_DEPT".equals(role) || "ROLE_LECTURER".equals(role)) {
                return "redirect:/lecturer/dashboard";
            } else if ("ROLE_STUDENT".equals(role)) {
                return "redirect:/student/dashboard";
            }
        }
        return "redirect:/login";
    }
}
