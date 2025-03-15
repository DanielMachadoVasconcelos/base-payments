package com.ead.payments.logging;

import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.security.Principal;
import java.util.List;
import lombok.SneakyThrows;
import lombok.extern.log4j.Log4j2;
import org.jboss.logging.MDC;
import org.springframework.context.annotation.Profile;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.User;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Log4j2
@Component
@Profile("!integration-test")
public class PrincipalLoggingInterceptor extends OncePerRequestFilter {

    @Override
    @SneakyThrows
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) {
        try {
            Principal principal = request.getUserPrincipal();

            switch (principal) {
                case UsernamePasswordAuthenticationToken token
                        when token.getPrincipal() instanceof User user -> {
                    MDC.put("principal", user.getUsername());
                    MDC.put("authorities", user.getAuthorities());
                }
                case UsernamePasswordAuthenticationToken token -> {
                    MDC.put("principal", token.getName());
                    MDC.put("authorities", token.getAuthorities());
                }
                default -> {
                    MDC.put("principal", "unknown");
                    MDC.put("authorities", List.of());
                }
            }
            filterChain.doFilter(request, response);
        } finally {
            MDC.clear();
        }
    }
}
