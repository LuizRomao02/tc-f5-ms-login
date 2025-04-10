package com.java.fiap.login.application.dto.form;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record ForgotPasswordForm(@Email @NotBlank String email) {}
