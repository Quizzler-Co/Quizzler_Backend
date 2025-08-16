package com.userauth.domain.dtos;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class UserIdentityAvailabilityDto {
    private boolean available;
}
