package com.ai.Resume.analyser.jwt;

import com.ai.Resume.analyser.configuration.entryPointService;
import com.ai.Resume.analyser.model.usersTable;
import com.ai.Resume.analyser.repository.usersTableRepo;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Service;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;


@Service
public class jwtFilter extends OncePerRequestFilter {


    @Autowired
    private entryPointService entryService;


    @Autowired
    private usersTableRepo usersTableRepository;


    @Autowired
    private jwtService jwtservice;



    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {


        try {


            String token = null;
            usersTable user = null;

            String reqUri = request.getRequestURI();



            // Public URLs
            if (reqUri.startsWith("/resumeAnalyser/entry/v1")
                    || reqUri.equals("/")
                    || reqUri.equals("/login")
                    || reqUri.equals("/forgotpassword")
                    || reqUri.startsWith("/oauth2/")
                    || reqUri.startsWith("/login/oauth2/")) {


                filterChain.doFilter(request, response);
                return;
            }



            // Get JWT Cookie

            Cookie[] cookies = request.getCookies();


            if(cookies != null){

                for(Cookie cookie : cookies){

                    if("entrypasstoken".equals(cookie.getName())){

                        token = cookie.getValue();
                        break;

                    }
                }
            }



            if(token != null){


                try {


                    String email = jwtservice.getEmail(token);


                    user = usersTableRepository
                            .findById(email)
                            .orElse(null);



                    if(user != null
                            && SecurityContextHolder
                            .getContext()
                            .getAuthentication() == null){



                        if(jwtservice.validateToken(token,user.getEmail())){


                            User userDetails =
                                    (User) entryService
                                            .loadUserByUsername(user.getEmail());



                            UsernamePasswordAuthenticationToken authentication =
                                    new UsernamePasswordAuthenticationToken(
                                            userDetails,
                                            null,
                                            userDetails.getAuthorities()
                                    );



                            authentication.setDetails(
                                    new WebAuthenticationDetailsSource()
                                            .buildDetails(request)
                            );



                            SecurityContextHolder
                                    .getContext()
                                    .setAuthentication(authentication);


                        }
                        else{


                            clearCookie(response);


                        }


                    }
                    else if(user == null){


                        clearCookie(response);


                    }


                }
                catch(Exception e){


                    clearCookie(response);


                }

            }



            filterChain.doFilter(request,response);



        }
        catch(Exception e){


            System.out.println(
                    "JWT Filter Error : "
                            + e.getMessage()
            );


            filterChain.doFilter(request,response);

        }


    }




    private void clearCookie(HttpServletResponse response){


        Cookie cookie =
                new Cookie("entrypasstoken","");


        cookie.setHttpOnly(true);

        cookie.setPath("/");

        cookie.setMaxAge(0);

        cookie.setSecure(false);


        response.addCookie(cookie);


    }



}
