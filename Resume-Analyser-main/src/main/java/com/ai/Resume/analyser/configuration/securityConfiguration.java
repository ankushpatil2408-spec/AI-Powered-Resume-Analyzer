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

                // JWT based application
                .csrf(AbstractHttpConfigurer::disable)


                // Enable CORS
                .cors(Customizer.withDefaults())


                // Authentication Provider
                .authenticationProvider(authenticationProvider())


                // API Permission
                .authorizeHttpRequests(auth -> auth

                        .requestMatchers(

                                // Auth APIs
                                "/resumeAnalyser/entry/v1/**",
                                "/login",
                                "/forgotpassword",

                                // Google OAuth
                                "/oauth2/**",
                                "/login/oauth2/**",

                                // Frontend
                                "/",
                                "/index.html",
                                "/static/**",
                                "/assets/**",
                                "/manifest.json"

                        )
                        .permitAll()


                        .anyRequest()
                        .authenticated()
                )


                // Disable default logout
                .logout(AbstractHttpConfigurer::disable)


                // JWT Filter
                .addFilterBefore(
                        jwtfilter,
                        UsernamePasswordAuthenticationFilter.class
                )


                // Google Login
                .oauth2Login(oauth -> oauth

                        .loginPage("/oauth2/authorization/google")

                        .successHandler(successHandler)

                        .failureHandler(failureHandler)

                )


                // Session required for OAuth2
                .sessionManagement(session ->

                        session.sessionCreationPolicy(
                                SessionCreationPolicy.IF_REQUIRED
                        )

                );


        return http.build();
    }



    @Bean
    public AuthenticationProvider authenticationProvider(){


        DaoAuthenticationProvider provider =
                new DaoAuthenticationProvider();


        provider.setUserDetailsService(userDetails);


        provider.setPasswordEncoder(
                passwordEncoder()
        );


        return provider;
    }



    @Bean
    public PasswordEncoder passwordEncoder(){

        return new BCryptPasswordEncoder(12);

    }

}
