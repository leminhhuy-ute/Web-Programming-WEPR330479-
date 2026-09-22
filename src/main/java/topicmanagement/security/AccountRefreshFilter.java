package topicmanagement.security;

import java.io.IOException;
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.web.filter.OncePerRequestFilter;
import topicmanagement.enums.UserStatus;
import topicmanagement.repository.UserRepository;

/** Enforce account locks and role changes on existing sessions, not only at login. */
public class AccountRefreshFilter extends OncePerRequestFilter {
    private final UserRepository users;
    private final SecurityContextRepository contexts;

    public AccountRefreshFilter(UserRepository users, SecurityContextRepository contexts) {
        this.users = users;
        this.contexts = contexts;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
            FilterChain chain) throws ServletException, IOException {
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof CurrentUser principal) {
            var user = users.findById(principal.id()).orElse(null);
            var context = SecurityContextHolder.createEmptyContext();
            if (user == null || user.getStatus() != UserStatus.ACTIVE) {
                var session = request.getSession(false);
                if (session != null) session.invalidate();
            } else {
                var fresh = CurrentUser.from(user);
                context.setAuthentication(UsernamePasswordAuthenticationToken.authenticated(
                    fresh, null, fresh.getAuthorities()));
            }
            SecurityContextHolder.setContext(context);
            contexts.saveContext(context, request, response);
        }
        chain.doFilter(request, response);
    }
}
