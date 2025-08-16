package com.userauth.domain.dtos;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class CurrentUserDto {
    private Long id;
    private String username;
    private String name;
    private String email;
    private Instant createdAt;
}
