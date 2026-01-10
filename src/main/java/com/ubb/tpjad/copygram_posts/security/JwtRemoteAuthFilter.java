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
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Optional;
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
        val token = extractBearer(authHeader).orElse(null);

        if (token == null) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Missing Bearer token");
            return;
        }

        RemoteAuthResult result;
        try {
            result = remoteAuthClient.validate(token);
        } catch (Exception e) {
            response.sendError(HttpServletResponse.SC_SERVICE_UNAVAILABLE, "Authorization service unavailable");
            return;
        }

        if (!result.valid()) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid Bearer token");
        }

        val authentication = new UsernamePasswordAuthenticationToken(
                result.userId(),
                null,
                Set.of(new SimpleGrantedAuthority(result.username()))
        );
        SecurityContextHolder.getContext().setAuthentication(authentication);
        filterChain.doFilter(request, response);
    }

    private Optional<String> extractBearer(String header) {
        if (header == null || !header.startsWith("Bearer ")) {
            return Optional.empty();
        }

        val token = header.substring(7).trim(); // "Bearer "
        return token.isEmpty() ? Optional.empty() : Optional.of(token);
    }
}
