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
  SecurityFilterChain securityFilterChain(HttpSecurity http, SecurityContextRepository contexts)
      throws Exception {
    CookieCsrfTokenRepository csrf = CookieCsrfTokenRepository.withHttpOnlyFalse();
    csrf.setCookiePath("/");
    CsrfTokenRequestAttributeHandler csrfHandler = new CsrfTokenRequestAttributeHandler();
    http
        .securityContext(c -> c.securityContextRepository(contexts).requireExplicitSave(false))
        .csrf(
            c -> c.csrfTokenRepository(csrf).csrfTokenRequestHandler(csrfHandler))
        .authorizeHttpRequests(
            a ->
                a.requestMatchers(
                        "/",
                        "/index.html",
                        "/login.html",
                        "/assets/**",
                        "/api/auth/login",
                        "/api/auth/csrf")
                    .permitAll()
                    .requestMatchers("/admin/**")
                    .hasRole("DEAN")
                    .requestMatchers("/api/admin/**")
                    .hasRole("DEAN")
                    .requestMatchers("/api/**")
                    .authenticated()
                    .anyRequest()
                    .permitAll())
        .exceptionHandling(
            e ->
                e.defaultAuthenticationEntryPointFor(
                        new HttpStatusEntryPoint(org.springframework.http.HttpStatus.UNAUTHORIZED),
                        new org.springframework.security.web.util.matcher.AntPathRequestMatcher("/api/**"))
                    .defaultAccessDeniedHandlerFor(
                        jsonDenied(),
                        new org.springframework.security.web.util.matcher.AntPathRequestMatcher(
                            "/api/**")))
        .sessionManagement(
            s -> s.sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED).maximumSessions(1));
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
