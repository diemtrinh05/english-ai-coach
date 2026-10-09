package com.example.englishaicoach.auth;

import com.example.englishaicoach.common.exception.ApiErrorCodes;
import com.example.englishaicoach.common.security.SecurityErrorWriter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

public final class JwtAuthenticationFilter extends OncePerRequestFilter {
    private final JwtAccessTokenService tokens;
    private final SecurityErrorWriter errors;

    public JwtAuthenticationFilter(JwtAccessTokenService tokens, SecurityErrorWriter errors) {
        this.tokens = tokens;
        this.errors = errors;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
            FilterChain chain) throws ServletException, IOException {
        List<String> headers = Collections.list(request.getHeaders("Authorization"));
        if (headers.isEmpty()) {
            chain.doFilter(request, response);
            return;
        }
        try {
            String header = headers.getFirst();
            if (headers.size() != 1 || header.length() < 8
                    || !header.regionMatches(true, 0, "Bearer ", 0, 7)) {
                throw new BadCredentialsException("Authorization không hợp lệ.");
            }
            AccessTokenIdentity identity = tokens.verify(header.substring(7));
            var authentication = UsernamePasswordAuthenticationToken.authenticated(identity, null,
                    List.of(new SimpleGrantedAuthority("ROLE_" + identity.role().name())));
            var context = SecurityContextHolder.createEmptyContext();
            context.setAuthentication(authentication);
            SecurityContextHolder.setContext(context);
        } catch (BadCredentialsException exception) {
            SecurityContextHolder.clearContext();
            errors.write(request, response, 401, ApiErrorCodes.UNAUTHORIZED,
                    "Phiên đăng nhập không hợp lệ hoặc đã hết hạn.");
            return;
        }
        chain.doFilter(request, response);
    }
}
