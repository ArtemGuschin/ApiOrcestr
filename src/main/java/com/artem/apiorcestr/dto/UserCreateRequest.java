package com.artem.apiorcestr.dto;



import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record UserCreateRequest(
        @NotBlank(message = "Username is required")
        String username,

        @Email(message = "Invalid email format")
        @NotBlank(message = "Email is required")
        String email,

        String firstName,
        String lastName,

        @NotBlank(message = "Password is required")
        String password,

        boolean enabled
) {}