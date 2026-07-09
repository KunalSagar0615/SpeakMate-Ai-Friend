package com.SpeakMate.Ai.friend.config;

import com.SpeakMate.Ai.friend.entities.User;
import com.SpeakMate.Ai.friend.enumeration.Role;
import com.SpeakMate.Ai.friend.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class AdminInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public AdminInitializer(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder
    ) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {

        boolean adminExists = userRepository.findAll()
                .stream()
                .anyMatch(user -> user.getRole() == Role.ADMIN);

        if (!adminExists) {

            User admin = new User();

            admin.setName("Kunal Ananda Sagar");
            admin.setUsername("Kunal.0615");
            admin.setEmail("kunalsagar0615@gmail.com");
            admin.setPassword(
                    passwordEncoder.encode("Kunal@0615")
            );

            admin.setRole(Role.ADMIN);
            admin.setEmailVerified(true);

            userRepository.save(admin);

            System.out.println("Admin user created successfully.");
        }
    }
}