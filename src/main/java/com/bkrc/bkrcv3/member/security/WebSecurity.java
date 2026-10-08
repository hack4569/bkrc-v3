package com.bkrc.bkrcv3.member.security;

import com.bkrc.bkrcv3.member.application.provided.MemberFinder;
import com.bkrc.bkrcv3.member.application.provided.MemberRegister;
import com.bkrc.bkrcv3.member.domain.PasswordEncoder;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class WebSecurity {
    private final MemberRegister userService;
    private final MemberFinder memberFinder;
    private final Environment env;
    private final ObjectMapper objectMapper;
    private final PasswordEncoder passwordEncoder;

    @Bean
    public SecurityFilterChain configure(HttpSecurity http) throws Exception {
        AuthenticationManagerBuilder authenticationManagerBuilder =
                http.getSharedObject(AuthenticationManagerBuilder.class);
        authenticationManagerBuilder.userDetailsService(memberFinder).passwordEncoder(passwordEncoder);

        AuthenticationManager authenticationManager = authenticationManagerBuilder.build();

        http.csrf( (csrf) -> csrf.disable())
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/v1/like/**",
                                "/v1/history/**",
                                "/v1/recommend/**",
                                "/v1/coupons/my/**"
                                ).authenticated()
                        // 쿠폰 목록은 공개하지만 다운로드와 보유 쿠폰 API는 회원 인증이 필요합니다.
                        .requestMatchers(HttpMethod.POST, "/v1/coupons/*/download").authenticated()
                        .requestMatchers(HttpMethod.GET, "/v1/coupons").permitAll()
                        .requestMatchers(HttpMethod.GET, "/v1/member/*").authenticated()
                        .requestMatchers(HttpMethod.PUT, "/v1/member/*").authenticated()
                        .requestMatchers(HttpMethod.GET, "/v1/aladin/books/search").authenticated()
                        .requestMatchers(HttpMethod.GET, "/v1/aladin/books/recommend/user").permitAll()
                        .requestMatchers(HttpMethod.POST, "/v1/aladin/books/recommend/user").authenticated()
                        .requestMatchers(HttpMethod.GET, "/v1/aladin/books/recommend/*").authenticated()
                        .requestMatchers(HttpMethod.PUT, "/v1/aladin/books/recommend/*").authenticated()
                        .anyRequest().permitAll()
                )
                .authenticationManager(authenticationManager)
                .addFilterBefore(getJwtAuthorizationFilter(), UsernamePasswordAuthenticationFilter.class)
                .addFilter(getAuthenticationFilter(authenticationManager))
                .headers((headers) -> headers
                        .frameOptions((frameOptions) -> frameOptions.sameOrigin()));

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOriginPatterns(List.of("*"));
//        config.setAllowedOrigins(List.of(
//                "http://localhost:63342",
//                "http://localhost:3000",          // 개발
//                "https://your-domain.com"         // 운영
//        ));
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("*"));
        config.setExposedHeaders(List.of("token")); // JWT 응답 헤더 노출
        config.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }

    private JwtAuthorizationFilter getJwtAuthorizationFilter() {
        return new JwtAuthorizationFilter(env);
    }

    private AuthenticationFilter getAuthenticationFilter(AuthenticationManager authenticationManager) throws Exception {
        AuthenticationFilter authenticationFilter =
                new AuthenticationFilter(authenticationManager, memberFinder, env, objectMapper);
        authenticationFilter.setAuthenticationManager(authenticationManager);

        return authenticationFilter;
    }
}
