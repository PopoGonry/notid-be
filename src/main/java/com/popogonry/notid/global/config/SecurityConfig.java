package com.popogonry.notid.global.config;


import com.popogonry.notid.global.jwt.JwtAuthenticationFilter;
import com.popogonry.notid.global.jwt.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import java.security.Security;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtTokenProvider jwtTokenProvider;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                // CSRF 보안 비활성화 (REST API 개발할 떄는 보통 끈다.
                .csrf(AbstractHttpConfigurer::disable)

                // HTTP Basic 인증 비활성화 (우리는 나중에 JWT 쓸 거니까)
                .httpBasic(AbstractHttpConfigurer::disable)

                // Form 로그인 비활성화 (API 서버니까 로그인 화면 필요 없음)
                .formLogin(AbstractHttpConfigurer::disable)

                // URL 별x 권한 관리
                .authorizeHttpRequests(auth -> auth
                        // 회원가입, 로그인 경로는 누구나 들어올 수 있게 허용
                        .requestMatchers("/api/users/signup", "/api/users/signin", "/h2-console/**").permitAll()
                        // 나머지는 인증된 사람만
                        .anyRequest().authenticated()
                )
                .addFilterBefore(new JwtAuthenticationFilter(jwtTokenProvider), UsernamePasswordAuthenticationFilter.class)
        ;

        return http.build();
    }

}
