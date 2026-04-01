package com.aureon.backend.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import com.aureon.backend.security.JwtAuthFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthFilter jwtAuthFilter;

    private static final String[] PUBLIC_PATHS = {
            "/api/v1/auth/**",
            "/swagger-ui/**",
            "/swagger-ui.html",
            "/api-docs/**",
            "/actuator/health"
    };

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(AbstractHttpConfigurer::disable)
            .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                // Public: auth endpoints + docs
                .requestMatchers(PUBLIC_PATHS).permitAll()

                // Dashboard: VIEWER and above can read
                .requestMatchers(HttpMethod.GET, "/api/v1/dashboard/**")
                    .hasAnyRole("VIEWER", "ANALYST", "ADMIN")

                // Records: GET allowed for ANALYST and ADMIN
                .requestMatchers(HttpMethod.GET, "/api/v1/records/**")
                    .hasAnyRole("ANALYST", "ADMIN")

                // Records: mutate only ADMIN
                .requestMatchers(HttpMethod.POST, "/api/v1/records/**")
                    .hasRole("ADMIN")
                .requestMatchers(HttpMethod.PUT, "/api/v1/records/**")
                    .hasRole("ADMIN")
                .requestMatchers(HttpMethod.PATCH, "/api/v1/records/**")
                    .hasRole("ADMIN")
                .requestMatchers(HttpMethod.DELETE, "/api/v1/records/**")
                    .hasRole("ADMIN")

                // User management: ADMIN only
                .requestMatchers("/api/v1/users/**").hasRole("ADMIN")

                .anyRequest().authenticated()
            )
            .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
