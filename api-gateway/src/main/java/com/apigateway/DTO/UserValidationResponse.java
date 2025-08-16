package com.apigateway.DTO;

import lombok.*;

@Data
@AllArgsConstructor
@RequiredArgsConstructor
@Setter
@Getter
public class UserValidationResponse {
    private String userId;
    private String email;
    private String role;

}
