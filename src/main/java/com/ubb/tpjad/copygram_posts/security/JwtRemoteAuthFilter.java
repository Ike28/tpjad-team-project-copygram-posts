package com.ubb.tpjad.copygram_posts.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Set;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtRemoteAuthFilter extends OncePerRequestFilter {
    private final RemoteAuthClient remoteAuthClient;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws IOException, ServletException {
        val authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Missing Bearer token");
            return;
        }

        RemoteAuthResult result;
        try {
            result = remoteAuthClient.validate(authHeader);
        } catch (Exception e) {
            log.error("Authorization service unavailable.", e);
            response.sendError(HttpServletResponse.SC_SERVICE_UNAVAILABLE, "Authorization service unavailable");
            return;
        }

        if (!result.valid()) {
            log.warn("Authorization of request with external service failed, invalid token.");
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid Bearer token");
            return;
        }

        log.debug("Request authorization succeeded for user {} with id {}.", result.username(), result.userId());
        val authentication = new UsernamePasswordAuthenticationToken(
                result.userId(),
                null,
                Set.of()
        );
        SecurityContextHolder.getContext().setAuthentication(authentication);
        log.info("Auth in filter: {}", SecurityContextHolder.getContext().getAuthentication());
        filterChain.doFilter(request, response);
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        val path = request.getRequestURI();
        val method = request.getMethod();

        if ("OPTIONS".equalsIgnoreCase(method)) return true;
        return path.startsWith("/actuator/health")
                || path.startsWith("/actuator/info")
                || path.startsWith("/v3/api-docs")
                || path.startsWith("/swagger-ui")
                || path.startsWith("/webjars");
    }
}
