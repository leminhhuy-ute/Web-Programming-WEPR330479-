package topicmanagement.config;

import org.springframework.context.annotation.*;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.*;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.*;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.csrf.CsrfTokenRequestAttributeHandler;

@Configuration
@EnableWebSecurity
public class SecurityConfig {
  @Bean
  PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
  }

  @Bean
  AuthenticationManager authenticationManager(AuthenticationProvider p) {
    return new ProviderManager(p);
  }

  @Bean
  SecurityContextRepository securityContextRepository() {
    return new HttpSessionSecurityContextRepository();
  }

  @Bean
  SecurityFilterChain securityFilterChain(HttpSecurity http, SecurityContextRepository contexts,
      topicmanagement.repository.UserRepository users)
      throws Exception {
    CookieCsrfTokenRepository csrf = CookieCsrfTokenRepository.withHttpOnlyFalse();
    csrf.setCookiePath("/");
    CsrfTokenRequestAttributeHandler csrfHandler = new CsrfTokenRequestAttributeHandler();
    http
        .addFilterBefore(new topicmanagement.security.AccountRefreshFilter(users, contexts),
            org.springframework.security.web.access.intercept.AuthorizationFilter.class)
        .securityContext(c -> c.securityContextRepository(contexts).requireExplicitSave(false))
        .csrf(
            c -> c.csrfTokenRepository(csrf).csrfTokenRequestHandler(csrfHandler))
        .authorizeHttpRequests(
            a ->
                a.requestMatchers(
                        "/",
                        "/index.html",
                        "/login.html",
                        "/login",
                        "/assets/**", "/css/**", "/js/**", "/images/**", "/error",
                        "/api/auth/login",
                        "/api/auth/csrf")
                    .permitAll()
                    .requestMatchers("/admin/**")
                    .hasRole("DEAN")
                    .requestMatchers("/api/admin/**")
                    .hasRole("DEAN")
                    .requestMatchers("/lecturer/**", "/api/lecturer/**", "/councils/**", "/api/councils/**")
                    .hasAnyRole("DEAN", "HEAD_OF_DEPT", "LECTURER")
                    .requestMatchers("/student", "/api/student/**")
                    .hasRole("STUDENT")
                    .requestMatchers("/api/**")
                    .authenticated()
                    .anyRequest()
                    .authenticated())
        .exceptionHandling(
            e ->
                e.authenticationEntryPoint((req, res, ex) -> {
                      if (req.getRequestURI().startsWith(req.getContextPath() + "/api/")) {
                        res.setStatus(401);
                        res.setContentType("application/json;charset=UTF-8");
                        res.getWriter().write("{\"success\":false,\"message\":\"Phiên đăng nhập đã hết hạn.\"}");
                      } else res.sendRedirect(req.getContextPath() + "/login.html");
                    })
                    .defaultAccessDeniedHandlerFor(
                        jsonDenied(),
                        new org.springframework.security.web.util.matcher.AntPathRequestMatcher(
                            "/api/**")))
        .sessionManagement(
            s -> s.sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED))
        .logout(l -> l.logoutUrl("/logout").logoutSuccessUrl("/login.html"));
    return http.build();
  }

  private AccessDeniedHandler jsonDenied() {
    return (req, res, ex) -> {
      res.setStatus(403);
      res.setContentType(MediaType.APPLICATION_JSON_VALUE);
      res.getWriter()
          .write("{\"success\":false,\"message\":\"Bạn không có quyền truy cập tài nguyên này.\"}");
    };
  }
}
