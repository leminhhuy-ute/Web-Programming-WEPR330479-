package vn.edu.hcmute.student;

import org.springframework.context.annotation.*;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.userdetails.*;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
class SecurityConfig {
    @Bean PasswordEncoder passwordEncoder() {return new BCryptPasswordEncoder();}
    @Bean UserDetailsService users(StudentRepository students) {
        return id -> students.findById(id).map(s->User.withUsername(s.id).password(s.passwordHash).roles("STUDENT").build())
            .orElseThrow(()->new UsernameNotFoundException("Không tìm thấy tài khoản"));
    }
    @Bean SecurityFilterChain security(HttpSecurity http) throws Exception {
        return http.authorizeHttpRequests(a->a.requestMatchers("/login","/css/**","/js/**","/images/**","/error").permitAll().anyRequest().hasRole("STUDENT"))
            .formLogin(f->f.loginPage("/login").defaultSuccessUrl("/student",true).permitAll())
            .logout(l->l.logoutSuccessUrl("/login?logout"))
            .exceptionHandling(e->e.authenticationEntryPoint((q,r,x)->{
                if(q.getRequestURI().startsWith("/api/")) r.sendError(401);
                else r.sendRedirect(q.getContextPath()+"/login");
            }))
            .build(); // CSRF protection remains enabled for every state-changing request.
    }
}
