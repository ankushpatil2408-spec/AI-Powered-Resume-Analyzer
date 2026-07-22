package com.ai.Resume.analyser.configuration;

import com.ai.Resume.analyser.model.usersTable;
import com.ai.Resume.analyser.repository.usersTableRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class entryPointService implements UserDetailsService {

    @Autowired
    private usersTableRepo usersTableRepository;

    @Override
    public UserDetails loadUserByUsername(String email)
            throws UsernameNotFoundException {

        System.out.println("========== LOGIN DEBUG ==========");
        System.out.println("Email Received : " + email);

        usersTable user = usersTableRepository.findById(email).orElse(null);

        if (user == null) {
            System.out.println("User NOT FOUND in Database");
            throw new UsernameNotFoundException("User not found with email: " + email);
        }

        System.out.println("User Found : " + user.getEmail());
        System.out.println("Password From DB : " + user.getPassword());

        return User.builder()
                .username(user.getEmail())
                .password(user.getPassword())
                .roles("USER")
                .build();
    }
}
