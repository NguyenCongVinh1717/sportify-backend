package SaleManagement.VinhNguyen.configuration;

import SaleManagement.VinhNguyen.entity.User;
import SaleManagement.VinhNguyen.enums.Role;
import SaleManagement.VinhNguyen.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
@RequiredArgsConstructor
public class DataInitializer {

    private final PasswordEncoder passwordEncoder;

    @Bean
    public CommandLineRunner initAdmin(UserRepository userRepository) {

        return args -> {

            if (userRepository.findByEmail("admin@gmail.com").isEmpty()) {

                User admin = User.builder()
                        .email("admin@gmail.com")
                        .fullName("Administrator")
                        .password(
                                passwordEncoder.encode("Admin2005@")
                        )
                        .role(Role.ROLE_ADMIN)
                        .enabled(true)
                        .build();

                userRepository.save(admin);

                System.out.println(">>> ADMIN CREATED");
            }
        };
    }
}