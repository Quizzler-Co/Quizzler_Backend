package com.userauth.controllers;

import com.userauth.domain.dtos.*;
import com.userauth.repository.UserRepository;
import com.userauth.security.CurrentUser;
import com.userauth.security.UserPrincipal;
import com.userauth.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class UserController {

    private final UserRepository userRepository;
    private final ModelMapper modelMapper;
    private final AuthService authService;

    @GetMapping("/users")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<List<UserDto>> getAllUsers() {

        List<UserDto> users = userRepository.findAll().stream()
                .map(u -> modelMapper.map(u, UserDto.class))
                .collect(Collectors.toList());

        return new ResponseEntity<>(users, HttpStatus.OK);
    }

    @GetMapping("/user")
    @PreAuthorize("hasAuthority('ROLE_USER')")
    public ResponseEntity<CurrentUserDto> getCurrentUser(@CurrentUser UserPrincipal currentUser) {
        try {
            if (currentUser == null) {
                throw new IllegalStateException("Current user is null");
            }
            CurrentUserDto me = modelMapper.map(currentUser, CurrentUserDto.class);
            return ResponseEntity.ok(me);
        } catch (Exception e) {
            // This will help us debug the issue
            throw new RuntimeException("Error mapping current user: " + e.getMessage(), e);
        }
    }

    @GetMapping("/user/checkUsernameAvailability")
    public ResponseEntity<UserIdentityAvailabilityDto> checkUsernameAvailability(
            @RequestParam("username") String username) {
        boolean check = userRepository.existsByUsername(username);
        return ResponseEntity.ok(new UserIdentityAvailabilityDto(check));
    }

    @GetMapping("/user/checkEmailAvailability")
    public ResponseEntity<UserIdentityAvailabilityDto> checkEmailAvailability(@RequestParam("email") String email) {
        boolean check = userRepository.existsByEmail(email);
        return ResponseEntity.ok(new UserIdentityAvailabilityDto(check));
    }

    @GetMapping("/user/debug")
    public ResponseEntity<String> debugCurrentUser(@CurrentUser UserPrincipal currentUser) {
        try {
            if (currentUser == null) {
                return ResponseEntity.ok("No authenticated user - JWT might be invalid or expired");
            }

            String authorities = currentUser.getAuthorities().stream()
                    .map(auth -> auth.getAuthority())
                    .collect(Collectors.joining(", "));

            return ResponseEntity.ok("User: " + currentUser.getUsername() +
                    ", Authorities: " + authorities +
                    ", ID: " + currentUser.getId() +
                    ", Name: " + currentUser.getName());
        } catch (Exception e) {
            return ResponseEntity.ok("Error in debug: " + e.getMessage());
        }
    }

    @PostMapping("/usernames")
    public ResponseEntity<?> getUsernamesByUserIds(
            @RequestBody List<Long> userIds,
            @RequestHeader("x-internal-call") String internalCall) {
        System.out.println("INTERNAL CALL HEADER = " + internalCall);
        if (!"true".equalsIgnoreCase(internalCall)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Access denied.");
        }


        Map<String, String> response = userRepository.findAllById(userIds).stream()
                .collect(Collectors.toMap(
                        user -> user.getId().toString(),
                        user -> user.getUsername()
                ));

        return ResponseEntity.ok(response);
    }


    @DeleteMapping("/user/{userId}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<ApiResponse> deleteUser(@PathVariable Long userId) {
        ApiResponse response = authService.deleteUser(userId);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/user/change-role")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<UserDto> changeUserRole(@Valid @RequestBody ChangeRoleRequest changeRoleRequest) {
        UserDto updatedUser = authService.changeUserRole(changeRoleRequest);
        return ResponseEntity.ok(updatedUser);
    }

}
