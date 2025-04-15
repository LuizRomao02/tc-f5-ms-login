package com.java.fiap.login.service.impl;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.java.fiap.login.domain.model.UserLogin;
import com.java.fiap.login.domain.repository.UserLoginRepository;
import com.java.fiap.login.service.EmailService;
import com.java.fiap.login.service.KeycloakAdminService;
import com.java.fiap.login.service.UserService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;

import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;

class UserServiceImplTest {

    AutoCloseable openMocks;
    private UserService userService;
    @Mock private KeycloakAdminService keycloakService;
    @Mock private UserLoginRepository loginRepository;
    @Mock private EmailService emailService;

    @BeforeEach
    void setUp() {
        openMocks = MockitoAnnotations.openMocks(this);
        this.userService = new UserServiceImpl(keycloakService, loginRepository, emailService);
    }

    @AfterEach
    void tearDown() throws Exception {
        openMocks.close();
    }

    @Nested
    class ForgotPassword {
        @Test
        void deveGerarExecaoQuandoEmailNaoEncontrado() {

            when(loginRepository.findByEmail(anyString())).thenReturn(null);

            assertThatThrownBy(() -> userService.forgotPassword(null))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessage("Invalid email");

            verify(loginRepository, times(1)).findByEmail(anyString());
            verify(loginRepository, never()).save(any(UserLogin.class));
        }
    }

    @Nested
    class ResetPassword {

    }

    @Nested
    class VerifyEmail {

    }
}
