package com.userauth.domain.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@AllArgsConstructor
@RequiredArgsConstructor
public class UserDto {
    private Long id;
    private String name;
    private String userName;
    private String email;

}
