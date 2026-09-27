package com.example.englishaicoach.config;

import com.example.englishaicoach.common.security.ClientIdentityResolver;
import com.example.englishaicoach.common.security.RateLimitFilter;
import com.example.englishaicoach.common.security.RateLimitGate;
import java.time.Clock;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.intercept.AuthorizationFilter;
import org.springframework.security.web.header.writers.ReferrerPolicyHeaderWriter;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import tools.jackson.databind.ObjectMapper;

@Configuration(proxyBeanMethods = false)
public class SecurityConfiguration {
    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http,
            UrlBasedCorsConfigurationSource corsSource, RateLimitProperties rateLimitProperties,
            RateLimitGate rateLimitGate, Clock clock, ObjectMapper mapper) throws Exception {
        RateLimitFilter rateLimitFilter = new RateLimitFilter(rateLimitProperties, rateLimitGate,
                clock, mapper, new ClientIdentityResolver(rateLimitProperties.trustedProxies()));
        http.cors(cors -> cors.configurationSource(corsSource))
                .csrf(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable)
                .formLogin(AbstractHttpConfigurer::disable)
                .logout(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                // Auth/RBAC sẽ được gắn bởi task identity; foundation này giữ behavior hiện tại.
                .authorizeHttpRequests(auth -> auth.anyRequest().permitAll())
                .headers(headers -> headers
                        .frameOptions(frame -> frame.deny())
                        .referrerPolicy(referrer -> referrer
                                .policy(ReferrerPolicyHeaderWriter.ReferrerPolicy.NO_REFERRER))
                        .contentSecurityPolicy(csp -> csp
                                .policyDirectives("default-src 'none'; frame-ancestors 'none'")))
                .addFilterBefore(rateLimitFilter, AuthorizationFilter.class);
        return http.build();
    }
}
