package com.jobflow.user_service.test;

import com.jobflow.user_service.user.Role;
import com.jobflow.user_service.user.User;
import com.jobflow.user_service.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;

@Configuration
@RequiredArgsConstructor
public class TestRunner implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        User firstUser = new User(null, "Ivan", "Ivanov", "ivanivanov@gmail.com", passwordEncoder.encode("abcde"), Role.ROLE_USER);
        User secondUser = new User(null, "Ivan", "Ivanov", "ivanivanov3@gmail.com", passwordEncoder.encode("abcde"), Role.ROLE_USER);

        userRepository.saveAll(List.of(firstUser, secondUser));
    }
}
