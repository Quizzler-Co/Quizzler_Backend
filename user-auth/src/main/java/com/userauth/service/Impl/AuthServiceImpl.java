package com.userauth.service.Impl;

import com.userauth.domain.Role;
import com.userauth.domain.User;
import com.userauth.domain.dtos.*;
import com.userauth.repository.RoleRepository;
import com.userauth.repository.UserRepository;
import com.userauth.security.JwtTokenProvider;
import com.userauth.service.AuthService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Collections;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider tokenProvider;
    private final PasswordEncoder passwordEncoder;
    private final RoleRepository roleRepository;
    private final ModelMapper modelMapper;

    @Override
    public JwtAuthenticationResponse login(LoginRequest loginRequest) {
        System.out.println("[AUTH-SERVICE] login() method called");
        System.out.println("[AUTH-SERVICE] Attempting authentication for: " + loginRequest.getUsernameOrEmail());
        
        try {
            // Authenticate user
            System.out.println("[AUTH-SERVICE] Calling authenticationManager.authenticate()...");
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            loginRequest.getUsernameOrEmail(),
                            loginRequest.getPassword()
                    )
            );
            System.out.println("[AUTH-SERVICE] Authentication successful!");

            SecurityContextHolder.getContext().setAuthentication(authentication);

            // Generate token
            System.out.println("[AUTH-SERVICE] Generating JWT token...");
            String token = tokenProvider.generateToken(authentication);
            System.out.println("[AUTH-SERVICE] Token generated successfully (length: " + token.length() + ")");

            // Get user from DB
            System.out.println("[AUTH-SERVICE] Fetching user from database...");
            User user = userRepository.findByUsernameOrEmail(
                    loginRequest.getUsernameOrEmail(),
                    loginRequest.getUsernameOrEmail()
            ).orElseThrow(() -> {
                System.err.println("[AUTH-SERVICE] User not found in database!");
                return new EntityNotFoundException("User not found with username or email: " + loginRequest.getUsernameOrEmail());
            });
            System.out.println("[AUTH-SERVICE] User found: " + user.getUsername() + " (ID: " + user.getId() + ")");

            // Return token + user info
            System.out.println("[AUTH-SERVICE] Creating JwtAuthenticationResponse...");
            JwtAuthenticationResponse response = new JwtAuthenticationResponse(
                    token,
                    "Bearer",
                    user.getName(),
                    user.getUsername(),
                    user.getEmail()
            );
            System.out.println("[AUTH-SERVICE] Response created successfully");
            return response;
        } catch (Exception e) {
            System.err.println("[AUTH-SERVICE] ERROR during login!");
            System.err.println("[AUTH-SERVICE] Exception type: " + e.getClass().getName());
            System.err.println("[AUTH-SERVICE] Exception message: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }

    @Override
    public UserDto signUp(SignUpRequest signUpRequest) {
        System.out.println("[AUTH-SERVICE] signUp() method called");
        System.out.println("[AUTH-SERVICE] Checking if email exists: " + signUpRequest.getEmail());
        
        if (userRepository.existsByEmail(signUpRequest.getEmail())) {
            System.err.println("[AUTH-SERVICE] Email already exists!");
            throw new IllegalArgumentException("This email is already registered");
        }
        System.out.println("[AUTH-SERVICE] Email is available");

        System.out.println("[AUTH-SERVICE] Checking if username exists: " + signUpRequest.getUsername());
        if (userRepository.existsByUsername(signUpRequest.getUsername())) {
            System.err.println("[AUTH-SERVICE] Username already exists!");
            throw new IllegalArgumentException("The username is already taken");
        }
        System.out.println("[AUTH-SERVICE] Username is available");

        System.out.println("[AUTH-SERVICE] Creating new User entity...");
        User newUser = new User();
        newUser.setName(signUpRequest.getName());
        newUser.setUsername(signUpRequest.getUsername());
        newUser.setEmail(signUpRequest.getEmail());
        newUser.setPassword(passwordEncoder.encode(signUpRequest.getPassword()));
        System.out.println("[AUTH-SERVICE] User entity created (password encoded)");

        System.out.println("[AUTH-SERVICE] Looking up role: " + signUpRequest.getRole());
        Role requestedRole = roleRepository.findByRole(signUpRequest.getRole())
                .orElseThrow(() -> {
                    System.err.println("[AUTH-SERVICE] Role not found: " + signUpRequest.getRole());
                    return new EntityNotFoundException("Requested role not found: " + signUpRequest.getRole());
                });
        System.out.println("[AUTH-SERVICE] Role found: " + requestedRole.getRole());

        newUser.setRoles(Collections.singleton(requestedRole));
        System.out.println("[AUTH-SERVICE] Saving user to database...");

        User savedUser = userRepository.save(newUser);
        System.out.println("[AUTH-SERVICE] User saved successfully! ID: " + savedUser.getId());
        System.out.println("[AUTH-SERVICE] User createdAt: " + savedUser.getCreatedAt());
        System.out.println("[AUTH-SERVICE] User updatedAt: " + savedUser.getUpdatedAt());
        
        System.out.println("[AUTH-SERVICE] Mapping User to UserDto using ModelMapper...");
        try {
            UserDto userDto = modelMapper.map(savedUser, UserDto.class);
            System.out.println("[AUTH-SERVICE] Mapping successful!");
            System.out.println("[AUTH-SERVICE] UserDto ID: " + userDto.getId());
            System.out.println("[AUTH-SERVICE] UserDto userName: " + userDto.getUserName());
            System.out.println("[AUTH-SERVICE] UserDto email: " + userDto.getEmail());
            return userDto;
        } catch (Exception e) {
            System.err.println("[AUTH-SERVICE] ERROR during ModelMapper mapping!");
            System.err.println("[AUTH-SERVICE] Exception type: " + e.getClass().getName());
            System.err.println("[AUTH-SERVICE] Exception message: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }

    @Override
    public ApiResponse resetPassword(ResetPasswordRequest resetPasswordRequest, Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found with id: " + userId));

        // Verify current password
        if (!passwordEncoder.matches(resetPasswordRequest.getCurrentPassword(), user.getPassword())) {
            throw new IllegalArgumentException("Current password is incorrect");
        }

        // Update password
        user.setPassword(passwordEncoder.encode(resetPasswordRequest.getNewPassword()));
        userRepository.save(user);

        return new ApiResponse(true, "Password reset successfully");
    }

    @Override
    public ApiResponse deleteUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found with id: " + userId));

        userRepository.delete(user);
        return new ApiResponse(true, "User deleted successfully");
    }

    @Override
    public UserDto changeUserRole(ChangeRoleRequest changeRoleRequest) {
        User user = userRepository.findById(changeRoleRequest.getUserId())
                .orElseThrow(() -> new EntityNotFoundException("User not found with id: " + changeRoleRequest.getUserId()));

        Role newRole = roleRepository.findByRole(changeRoleRequest.getNewRole())
                .orElseThrow(() -> new EntityNotFoundException("Role not found: " + changeRoleRequest.getNewRole()));

        user.setRoles(Collections.singleton(newRole));
        User updatedUser = userRepository.save(user);

        return modelMapper.map(updatedUser, UserDto.class);
    }
}
