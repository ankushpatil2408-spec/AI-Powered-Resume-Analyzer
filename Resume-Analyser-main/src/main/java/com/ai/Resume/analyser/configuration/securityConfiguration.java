package com.ai.Resume.analyser.configuration;

import com.ai.Resume.analyser.jwt.jwtFilter;
import com.ai.Resume.analyser.service.failureHandler;
import com.ai.Resume.analyser.service.successHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
@EnableWebSecurity
public class securityConfiguration {

    @Autowired
    private entryPointService userDetails;

    @Autowired
    private jwtFilter jwtfilter;

    @Autowired
    private successHandler successHandler;

    @Autowired
    private failureHandler failureHandler;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

        http
                .csrf(AbstractHttpConfigurer::disable)

                .cors(Customizer.withDefaults())

                .authenticationProvider(authenticationProvider())

                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/resumeAnalyser/entry/v1/**",
                                "/login",
                                "/forgotpassword",
                                "/oauth2/**",
                                "/login/oauth2/**",
                                "/",
                                "/index.html",
                                "/assets/**",
                                "/static/**",
                                "/manifest.json",
                                "/favicon.ico"
                        ).permitAll()
                        .anyRequest().authenticated()
                )

                .oauth2Login(oauth -> oauth
                        .loginPage("/oauth2/authorization/google")
                        .successHandler(successHandler)
                        .failureHandler(failureHandler)
                )

                .logout(AbstractHttpConfigurer::disable)

                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED)
                )

                .addFilterBefore(jwtfilter,
                        UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public AuthenticationProvider authenticationProvider() {

        DaoAuthenticationProvider provider =
                new DaoAuthenticationProvider();

        provider.setUserDetailsService(userDetails);
        provider.setPasswordEncoder(passwordEncoder());

        return provider;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(12);
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {

        CorsConfiguration configuration = new CorsConfiguration();

        configuration.setAllowedOrigins(List.of(
                "https://ai-powered-resume-analyzer-wjad.onrender.com"
        ));

        configuration.setAllowedMethods(List.of(
                "GET",
                "POST",
                "PUT",
                "DELETE",
                "OPTIONS"
        ));

        configuration.setAllowedHeaders(List.of("*"));

        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source =
                new UrlBasedCorsConfigurationSource();

        source.registerCorsConfiguration("/**", configuration);

        return source;
    }
}
