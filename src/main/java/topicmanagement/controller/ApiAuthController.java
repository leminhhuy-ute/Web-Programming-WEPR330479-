package topicmanagement.controller;

import jakarta.servlet.http.*;
import jakarta.validation.Valid;
import org.springframework.http.*;
import org.springframework.security.authentication.*;
import org.springframework.security.core.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.*;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.bind.annotation.*;
import topicmanagement.dto.*;
import topicmanagement.dto.request.LoginRequest;
import topicmanagement.security.CurrentUser;

@RestController
@RequestMapping("/api/auth")
public class ApiAuthController {
  private final AuthenticationManager auth;
  private final SecurityContextRepository contexts;

  public ApiAuthController(AuthenticationManager a, SecurityContextRepository c) {
    auth = a;
    contexts = c;
  }

  @PostMapping("/login")
  public ResponseEntity<ApiResponse<CurrentUser>> login(
      @Valid @RequestBody LoginRequest body, HttpServletRequest req, HttpServletResponse res) {
    Authentication result =
        auth.authenticate(
            UsernamePasswordAuthenticationToken.unauthenticated(body.username(), body.password()));
    SecurityContext context = SecurityContextHolder.createEmptyContext();
    context.setAuthentication(result);
    SecurityContextHolder.setContext(context);
    contexts.saveContext(context, req, res);
    return ResponseEntity.ok(
        ApiResponse.ok("Đăng nhập thành công.", (CurrentUser) result.getPrincipal()));
  }

  @PostMapping("/logout")
  public ApiResponse<Void> logout(
      HttpServletRequest req, HttpServletResponse res, Authentication a) {
    new SecurityContextLogoutHandler().logout(req, res, a);
    return ApiResponse.ok("Đã đăng xuất.", null);
  }

  @GetMapping("/me")
  public ApiResponse<CurrentUser> me(@AuthenticationPrincipal CurrentUser user) {
    return ApiResponse.ok("Phiên đăng nhập hợp lệ.", user);
  }

  @GetMapping("/csrf")
  public ApiResponse<String> csrf(CsrfToken token) {
    return ApiResponse.ok("CSRF token đã sẵn sàng.", token.getToken());
  }
}
