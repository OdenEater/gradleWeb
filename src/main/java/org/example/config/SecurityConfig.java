package org.example.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http.securityContext(securityContext -> securityContext.requireExplicitSave(false)).csrf(csrf -> csrf.disable()) // CSRFトークン検証を無効化
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .csrf(csrf -> csrf.disable())
                .headers(headers -> headers.contentSecurityPolicy(csp -> csp.policyDirectives("frame-ancestors 'self'")))
                // リクエスト認可ルール
                .authorizeHttpRequests(auth -> auth.requestMatchers("/css/**", "/js/**", "/ico/**", "/error.html", "/login.html", "/csrf", "/api/users").permitAll().requestMatchers("/api/users/**").authenticated().anyRequest().authenticated())
                // フォームログイン設定
                .formLogin(form -> form.loginPage("/login.html").loginProcessingUrl("/api/login").defaultSuccessUrl("/home.html", true).failureUrl("/login.html?error=true").permitAll())
                // ログアウト設定
                .logout(logout -> logout.logoutUrl("/api/logout").logoutSuccessUrl("/login.html").permitAll()).exceptionHandling(exception -> exception.authenticationEntryPoint((request, response, authException) -> {
                    if (request.getRequestURI().startsWith("/api/")) {
                        response.setStatus(HttpStatus.UNAUTHORIZED.value());
                        response.setContentType("application/json;charset=UTF-8");
                        response.getWriter().write("{\"message\":\"認証が必要です\"}");
                    } else {
                        response.sendRedirect("/login.html");
                    }
                }).accessDeniedHandler((request, response, accessDeniedException) -> {
                    if (request.getRequestURI().startsWith("/api/")) {
                        response.setStatus(HttpStatus.FORBIDDEN.value());
                        response.setContentType("application/json;charset=UTF-8");
                        response.getWriter().write("{\"message\":\"権限がありません\"}");
                    } else {
                        response.sendRedirect("/error.html");
                    }
                }));

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(Arrays.asList("http://localhost", "http://127.0.0.1"));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST","PUT"));
        configuration.setAllowCredentials(true);
        configuration.setAllowedHeaders(Arrays.asList("Authorization", "Cache-Control", "Content-Type"));
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}