package com.aistarter.auth.security;

import com.aistarter.common.constant.AppConstants;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http.csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/actuator/**").permitAll()
                        .requestMatchers("/", "/index.html", "/chat", "/rag", "/prompts", "/tools", "/login").permitAll()
                        .requestMatchers("/assets/**", "/static/**").permitAll()
                        .requestMatchers("/doc.html", "/webjars/**", "/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html").permitAll()
                        .requestMatchers(HttpMethod.POST, AppConstants.API_PREFIX + "/auth/login").permitAll()
                        .requestMatchers(HttpMethod.POST, AppConstants.API_PREFIX + "/chat").permitAll()
                        .requestMatchers(HttpMethod.POST, AppConstants.API_PREFIX + "/chat/stream").permitAll()
                        .requestMatchers(HttpMethod.GET, AppConstants.API_PREFIX + "/tools").permitAll()
                        .requestMatchers(HttpMethod.POST, AppConstants.API_PREFIX + "/agent/**").permitAll()
                        .requestMatchers(HttpMethod.POST, AppConstants.API_PREFIX + "/workflows/**").permitAll()
                        .requestMatchers(AppConstants.API_PREFIX + "/rag/**").permitAll()
                        .requestMatchers(AppConstants.API_PREFIX + "/prompts/**").permitAll()
                        .anyRequest().authenticated())
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }
}
