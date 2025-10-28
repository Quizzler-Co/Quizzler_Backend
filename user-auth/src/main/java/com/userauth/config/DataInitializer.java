package com.userauth.config;

import com.userauth.domain.Role;
import com.userauth.domain.RoleName;
import com.userauth.domain.User;
import com.userauth.repository.RoleRepository;
import com.userauth.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Set;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        // Initialize roles if they don't exist
        Role userRole;
        Role adminRole;
        
        if (roleRepository.findByRole(RoleName.ROLE_USER).isEmpty()) {
            userRole = new Role();
            userRole.setRole(RoleName.ROLE_USER);
            userRole = roleRepository.save(userRole);
            System.out.println("Created ROLE_USER");
        } else {
            userRole = roleRepository.findByRole(RoleName.ROLE_USER).orElseThrow();
        }

        if (roleRepository.findByRole(RoleName.ROLE_ADMIN).isEmpty()) {
            adminRole = new Role();
            adminRole.setRole(RoleName.ROLE_ADMIN);
            adminRole = roleRepository.save(adminRole);
            System.out.println("Created ROLE_ADMIN");
        } else {
            adminRole = roleRepository.findByRole(RoleName.ROLE_ADMIN).orElseThrow();
        }

        // Create super admin user if it doesn't exist
        String superAdminEmail = "admin@quizzler.com";
        String superAdminUsername = "admin";
        String superAdminPassword = "Admin@123"; // Default password - CHANGE THIS IN PRODUCTION!
        
        boolean emailExists = userRepository.existsByEmail(superAdminEmail);
        boolean usernameExists = userRepository.existsByUsername(superAdminUsername);
        
        if (emailExists || usernameExists) {
            System.out.println("Super admin user already exists - skipping creation");
            if (emailExists) {
                System.out.println("User with email '" + superAdminEmail + "' already exists");
            }
            if (usernameExists) {
                System.out.println("User with username '" + superAdminUsername + "' already exists");
            }
        } else {
            try {
                User superAdmin = new User();
                superAdmin.setName("Super Admin");
                superAdmin.setUsername(superAdminUsername);
                superAdmin.setEmail(superAdminEmail);
                superAdmin.setPassword(passwordEncoder.encode(superAdminPassword));
                
                // Set both roles for super admin
                Set<Role> roles = new HashSet<>();
                roles.add(userRole);
                roles.add(adminRole);
                superAdmin.setRoles(roles);
                
                userRepository.save(superAdmin);
                System.out.println("=========================================");
                System.out.println("Super Admin Created Successfully!");
                System.out.println("Email: " + superAdminEmail);
                System.out.println("Username: " + superAdminUsername);
                System.out.println("Password: " + superAdminPassword);
                System.out.println("=========================================");
                System.out.println("WARNING: Change default password in production!");
                System.out.println("=========================================");
            } catch (Exception e) {
                // Handle any unique constraint violation or other errors
                System.err.println("Error creating super admin: " + e.getMessage());
                System.err.println("Super admin may already exist or there was a database error");
            }
        }
    }
}
