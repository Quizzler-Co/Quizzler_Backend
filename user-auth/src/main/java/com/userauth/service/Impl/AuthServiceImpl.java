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
        // Authenticate user
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        loginRequest.getUsernameOrEmail(),
                        loginRequest.getPassword()
                )
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);

        // Generate token
        String token = tokenProvider.generateToken(authentication);

        // Get user from DB
        User user = userRepository.findByUsernameOrEmail(
                loginRequest.getUsernameOrEmail(),
                loginRequest.getUsernameOrEmail()
        ).orElseThrow(() ->
                new EntityNotFoundException("User not found with username or email: " + loginRequest.getUsernameOrEmail())
        );

        // Return token + user info
        return new JwtAuthenticationResponse(
                token,
                "Bearer",
                user.getName(),
                user.getUsername(),
                user.getEmail()
        );
    }

    @Override
    public UserDto signUp(SignUpRequest signUpRequest) {
        if (userRepository.existsByEmail(signUpRequest.getEmail())) {
            throw new IllegalArgumentException("This email is already registered");
        }

        if (userRepository.existsByUsername(signUpRequest.getUsername())) {
            throw new IllegalArgumentException("The username is already taken");
        }

        User newUser = new User();
        newUser.setName(signUpRequest.getName());
        newUser.setUsername(signUpRequest.getUsername());
        newUser.setEmail(signUpRequest.getEmail());
        newUser.setPassword(passwordEncoder.encode(signUpRequest.getPassword()));

        Role requestedRole = roleRepository.findByRole(signUpRequest.getRole())
                .orElseThrow(() -> new EntityNotFoundException("Requested role not found: " + signUpRequest.getRole()));

        newUser.setRoles(Collections.singleton(requestedRole));

        User savedUser = userRepository.save(newUser);
        return modelMapper.map(savedUser, UserDto.class);
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
