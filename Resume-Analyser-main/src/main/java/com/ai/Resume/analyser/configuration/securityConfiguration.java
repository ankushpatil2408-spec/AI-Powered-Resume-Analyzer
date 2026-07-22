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
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;


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
                // Disable CSRF because we are using JWT
                .csrf(AbstractHttpConfigurer::disable)


                // Allow frontend requests
                .cors(Customizer.withDefaults())


                // URL Authorization
                .authorizeHttpRequests(auth -> auth

                        // Public APIs
                        .requestMatchers(
                                "/resumeAnalyser/entry/v1/**",
                                "/",
                                "/login",
                                "/forgotpassword",

                                // Google OAuth URLs
                                "/oauth2/**",
                                "/login/oauth2/**",

                                // Frontend resources
                                "/static/**",
                                "/index.html",
                                "/manifest.json",
                                "/assets/**"
                        )
                        .permitAll()


                        // Other APIs require authentication
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


                // OAuth needs session
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
                new DaoAuthenticationProvider(userDetails);


        provider.setPasswordEncoder(
                new BCryptPasswordEncoder(12)
        );


        return provider;
    }

}
