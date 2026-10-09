package com.example.englishaicoach.config;

import com.example.englishaicoach.common.security.ClientIdentityResolver;
import com.example.englishaicoach.common.security.RateLimitFilter;
import com.example.englishaicoach.common.security.RateLimitGate;
import com.example.englishaicoach.common.security.SecurityErrorWriter;
import com.example.englishaicoach.common.exception.ApiErrorCodes;
import com.example.englishaicoach.auth.JwtAccessTokenService;
import com.example.englishaicoach.auth.JwtAuthenticationFilter;
import java.time.Clock;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.intercept.AuthorizationFilter;
import org.springframework.security.web.authentication.AnonymousAuthenticationFilter;
import org.springframework.http.HttpMethod;
import org.springframework.security.web.header.writers.ReferrerPolicyHeaderWriter;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import tools.jackson.databind.ObjectMapper;

@Configuration(proxyBeanMethods = false)
public class SecurityConfiguration {
    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http,
            UrlBasedCorsConfigurationSource corsSource, RateLimitProperties rateLimitProperties,
            RateLimitGate rateLimitGate, Clock clock, ObjectMapper mapper,
            JwtAccessTokenService tokens) throws Exception {
        RateLimitFilter rateLimitFilter = new RateLimitFilter(rateLimitProperties, rateLimitGate,
                clock, mapper, new ClientIdentityResolver(rateLimitProperties.trustedProxies()));
        SecurityErrorWriter errors = new SecurityErrorWriter(clock, mapper);
        http.cors(cors -> cors.configurationSource(corsSource))
                .csrf(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable)
                .formLogin(AbstractHttpConfigurer::disable)
                .logout(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .exceptionHandling(exceptions -> exceptions
                        .authenticationEntryPoint((request, response, exception) -> errors.write(
                                request, response, 401, ApiErrorCodes.UNAUTHORIZED,
                                "Bạn cần đăng nhập để thực hiện yêu cầu này."))
                        .accessDeniedHandler((request, response, exception) -> errors.write(
                                request, response, 403, ApiErrorCodes.FORBIDDEN,
                                "Bạn không có quyền thực hiện yêu cầu này.")))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.POST, "/api/v1/auth/register", "/api/v1/auth/login",
                                "/api/v1/auth/refresh", "/api/v1/auth/google").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/v1/goals", "/api/v1/cefr-levels",
                                "/api/v1/topics", "/api/v1/topics/{topicId}",
                                "/api/v1/vocabulary", "/api/v1/vocabulary/{vocabularyId}",
                                "/api/v1/vocabulary/{vocabularyId}/examples").permitAll()
                        .requestMatchers("/api/v1/admin/**").hasRole("ADMIN")
                        .requestMatchers("/api/v1/**").authenticated()
                        .anyRequest().permitAll())
                .headers(headers -> headers
                        .frameOptions(frame -> frame.deny())
                        .referrerPolicy(referrer -> referrer
                                .policy(ReferrerPolicyHeaderWriter.ReferrerPolicy.NO_REFERRER))
                        .contentSecurityPolicy(csp -> csp
                                .policyDirectives("default-src 'none'; frame-ancestors 'none'")))
                .addFilterBefore(new JwtAuthenticationFilter(tokens, errors), AnonymousAuthenticationFilter.class)
                .addFilterBefore(rateLimitFilter, AuthorizationFilter.class);
        return http.build();
    }
}
