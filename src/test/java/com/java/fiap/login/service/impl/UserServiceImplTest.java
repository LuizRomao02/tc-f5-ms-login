package com.java.fiap.login.service.impl;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

import com.java.fiap.login.application.dto.UserKeycloak;
import com.java.fiap.login.application.dto.UserRegisteredEvent;
import com.java.fiap.login.application.dto.form.NewPasswordForm;
import com.java.fiap.login.domain.model.UserLogin;
import com.java.fiap.login.domain.repository.UserLoginRepository;
import com.java.fiap.login.service.EmailService;
import com.java.fiap.login.service.KeycloakAdminService;
import com.java.fiap.login.service.UserService;
import com.java.fiap.login.utils.UserHelper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

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
    class CreateUser {
        @Test
        void deveCriarUser() {
            UserRegisteredEvent userRegisteredEvent = UserHelper.gerarUserRegisteredEvent();
            UserKeycloak userKeycloak = UserHelper.gerarUserKeycloak();

            when(keycloakService.createKeycloakUser(any(UserLogin.class), anyString()))
                    .thenReturn(userKeycloak);
            when(loginRepository.save(any(UserLogin.class))).thenReturn(UserHelper.gerarUserLogin());
            doNothing().when(keycloakService).addRoleToUser(any(UserLogin.class));

            userService.createUser(userRegisteredEvent);

            verify(keycloakService, times(1)).createKeycloakUser(any(UserLogin.class), anyString());
            verify(loginRepository, times(1)).save(any(UserLogin.class));
            verify(keycloakService, times(1)).addRoleToUser(any(UserLogin.class));
        }
    }

    @Nested
    class ForgotPassword {
        @Test
        void deveGerarExcecaoQuandoEmailNaoEncontrado() {
            
            when(loginRepository.findByEmail(anyString())).thenReturn(null);

            assertThatThrownBy(() -> userService.forgotPassword(UserHelper.EMAIL))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessage("Invalid email");

            verify(loginRepository, times(1)).findByEmail(anyString());
            verify(loginRepository, never()).save(any(UserLogin.class));
        }

        @Test
        void deveEnviarNotificacaoQuandoEmailEncontrado() {

            UserLogin userLogin = UserHelper.gerarUserLogin();

            when(loginRepository.findByEmail(anyString())).thenReturn(userLogin);

            userService.forgotPassword(UserHelper.EMAIL);

            verify(loginRepository, times(1)).findByEmail(anyString());
            verify(loginRepository, times(1)).save(any(UserLogin.class));
        }
    }

    @Nested
    class ResetPassword {
        @Test
        void deveGerarExcecaoQuandoTokenNaoEncontrado() {
            String token = "token123";
            NewPasswordForm newPasswordForm = new NewPasswordForm("newPassword");

            when(loginRepository.findByTokenVerification(anyString())).thenReturn(null);

            assertThatThrownBy(() -> userService.resetPassword(token, newPasswordForm))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessage("Invalid Token");

            verify(loginRepository, times(1)).findByTokenVerification(anyString());
            verify(keycloakService, never()).resetPassword(any(UserLogin.class), anyString());
            verify(loginRepository, never()).save(any(UserLogin.class));
        }

        @Test
        void deveGerarNovaSenhaQuandoTokenEncontrado() {
            UserLogin userLogin = UserHelper.gerarUserLogin();
            String token = "token123";
            NewPasswordForm newPasswordForm = new NewPasswordForm("newPassword");

            when(loginRepository.findByTokenVerification(anyString())).thenReturn(userLogin);

            userService.resetPassword(token, newPasswordForm);

            verify(loginRepository, times(1)).findByTokenVerification(anyString());
            verify(keycloakService, times(1)).resetPassword(any(UserLogin.class), anyString());
            verify(loginRepository, times(1)).save(any(UserLogin.class));
        }
    }

    @Nested
    class ValidateEmailUser {
        @Test
        void deveGerarExcecaoQuandoTokenNaoEncontrado() {
            String token = "token123";

            when(loginRepository.findByTokenVerification(anyString())).thenReturn(null);

            assertThatThrownBy(() -> userService.validateEmailUser(token))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessage("Invalid token");

            verify(loginRepository, times(1)).findByTokenVerification(anyString());
            verify(keycloakService, never()).confirmEmail(any(UserLogin.class));
        }

        @Test
        void deveConfirmarEmailQuandoTokenEncontrado() {
            UserLogin userLogin = UserHelper.gerarUserLogin();
            String token = "token123";

            when(loginRepository.findByTokenVerification(anyString())).thenReturn(userLogin);

            userService.validateEmailUser(token);

            verify(loginRepository, times(1)).findByTokenVerification(anyString());
            verify(keycloakService, times(1)).confirmEmail(any(UserLogin.class));
        }
    }
}
