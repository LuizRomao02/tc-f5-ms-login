package com.java.fiap.login.application.dto.form;

import jakarta.validation.constraints.NotBlank;

public record NewPasswordForm(@NotBlank String newPassword) {}
