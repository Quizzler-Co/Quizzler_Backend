package com.userauth.domain.dtos;

import com.userauth.domain.RoleName;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ChangeRoleRequest {

    @NotNull
    private Long userId;

    @NotNull
    private RoleName newRole;
}
