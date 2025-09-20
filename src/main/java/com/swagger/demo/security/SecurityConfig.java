package com.swagger.demo.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private static final String[] SWAGGER_WHITELIST = {
        "/v3/api-docs/**",
        "/swagger-ui/**",
        "/swagger-ui.html",
        "/index.html"
    };

    @Bean
    @Profile("!prod")
    public SecurityFilterChain devSecurityFilterChain(HttpSecurity http) throws Exception {
        // Lenient configuration for development and testing
        http
                .csrf(csrf -> csrf
                .csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
                // Disable CSRF for API and H2 console for easier testing
                .ignoringRequestMatchers("/api/**", "/h2-console/**")
                )
                // Allow H2 console to be embedded in a frame
                .headers(headers -> headers.frameOptions().sameOrigin())
                .authorizeHttpRequests(authorize -> authorize
                .requestMatchers("/", "/index.html").permitAll()
                .requestMatchers(SWAGGER_WHITELIST).permitAll()
                .requestMatchers("/h2-console/**").permitAll()
                .anyRequest().authenticated()
                )
                .formLogin(Customizer.withDefaults())
                .httpBasic(Customizer.withDefaults());

        return http.build();
    }

    @Bean
    @Profile("prod")
    public SecurityFilterChain prodSecurityFilterChain(HttpSecurity http) throws Exception {
        // Stricter configuration for production
        http
                .csrf(csrf -> csrf
                // Use a cookie-based repository, common for SPAs.
                .csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
                // In production, CSRF is enabled for all state-changing requests by default.
                )
                .authorizeHttpRequests(authorize -> authorize
                // Publicly accessible home page
                .requestMatchers("/", "/index.html").permitAll()
                // NOTE: You might want to disable Swagger in a real production environment.
                .requestMatchers(SWAGGER_WHITELIST).permitAll()
                // All other requests must be authenticated
                .anyRequest().authenticated()
                )
                // Use form login for user interaction
                .formLogin(Customizer.withDefaults());
        // HTTP Basic is not enabled for production

        return http.build();
    }

    @Bean
    public UserDetailsService userDetailsService() {
        UserDetails user = User.builder()
                .username("user")
                .password(passwordEncoder().encode("password"))
                .roles("USER")
                .build();
        return new InMemoryUserDetailsManager(user);
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
