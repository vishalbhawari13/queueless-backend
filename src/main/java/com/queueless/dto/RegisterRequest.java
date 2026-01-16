package com.queueless.dto;

import jakarta.validation.constraints.*;
import lombok.*;

@Getter @Setter
public class RegisterRequest {

    @NotBlank
    private String name;

    @Email
    private String email;

    @Pattern(regexp = "^[6-9][0-9]{9}$")
    private String phone;

    @Size(min = 6)
    private String password;
}
