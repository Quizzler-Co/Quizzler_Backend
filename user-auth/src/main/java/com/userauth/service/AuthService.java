package com.userauth.service;

import com.userauth.domain.dtos.*;

public interface AuthService {

    JwtAuthenticationResponse login(LoginRequest loginRequest);

    UserDto signUp(SignUpRequest signUpRequest);

    ApiResponse resetPassword(ResetPasswordRequest resetPasswordRequest, Long userId);

    ApiResponse deleteUser(Long userId);

    UserDto changeUserRole(ChangeRoleRequest changeRoleRequest);

}
