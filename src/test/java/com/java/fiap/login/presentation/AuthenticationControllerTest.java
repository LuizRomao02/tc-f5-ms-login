package com.java.fiap.login.presentation;

import static com.java.fiap.login.utils.JsonUtil.toJson;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.callibrity.logging.test.LogTracker;
import com.callibrity.logging.test.LogTrackerStub;
import com.java.fiap.login.application.dto.InfoUserKeycloakDTO;
import com.java.fiap.login.application.dto.UserLoginDTO;
import com.java.fiap.login.application.dto.form.ForgotPasswordForm;
import com.java.fiap.login.application.dto.form.NewPasswordForm;
import com.java.fiap.login.service.KeycloakAdminService;
import com.java.fiap.login.service.UserService;
import com.java.fiap.login.utils.UserHelper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.token.TokenManager;
import org.keycloak.representations.AccessTokenResponse;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

class AuthenticationControllerTest {

    @RegisterExtension
    LogTrackerStub logTracker =
            LogTrackerStub.create()
                    .recordForLevel(LogTracker.LogLevel.INFO)
                    .recordForType(AuthenticationController.class);

    private MockMvc mockMvc;
    private AutoCloseable openMocks;

    @Mock
    private KeycloakAdminService keycloakAdminService;
    @Mock
    private UserService userService;

    @BeforeEach
    void setUp() {
        this.openMocks = MockitoAnnotations.openMocks(this);
        AuthenticationController productController = new AuthenticationController(keycloakAdminService, userService);
        mockMvc =
                MockMvcBuilders.standaloneSetup(productController)
                        .addFilter(
                                (req, resp, chain) -> {
                                    resp.setCharacterEncoding("UTF-8");
                                    chain.doFilter(req, resp);
                                },
                                "/*")
                        .build();
    }

    @AfterEach
    void tearDown() throws Exception {
        openMocks.close();
    }

    @Test
    void deveLogin() throws Exception {
        UserLoginDTO userLoginDTO = UserHelper.gerarUserLoginDTO();

        Keycloak keycloak = mock(Keycloak.class);
        TokenManager tokenManager = mock(TokenManager.class);
        AccessTokenResponse accessTokenResponse = mock(AccessTokenResponse.class);

        when(keycloakAdminService.getKeycloakUser(any(UserLoginDTO.class))).thenReturn(keycloak);
        when(keycloak.tokenManager()).thenReturn(tokenManager);
        when(tokenManager.getAccessToken()).thenReturn(accessTokenResponse);
        when(accessTokenResponse.getToken()).thenReturn("token");

        mockMvc
                .perform(
                        post("/auth/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(toJson(userLoginDTO)))
                .andExpect(status().isOk());

        verify(keycloakAdminService, times(1)).getKeycloakUser(any(UserLoginDTO.class));
    }

    @Test
    void deveLogout() throws Exception {
        InfoUserKeycloakDTO userKeycloakDTO = UserHelper.gerarInfoUserKeycloakDTO();

        doNothing().when(keycloakAdminService).logout(any(InfoUserKeycloakDTO.class));

        mockMvc
                .perform(
                        post("/auth/logout")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(toJson(userKeycloakDTO)))
                .andExpect(status().isOk());

        verify(keycloakAdminService, times(1)).logout(any(InfoUserKeycloakDTO.class));
    }

    @Test
    void deveForgotPassword() throws Exception {
        ForgotPasswordForm forgotPasswordForm = new ForgotPasswordForm("email@gmail.com");

        doNothing().when(userService).forgotPassword(anyString());

        mockMvc
                .perform(
                        put("/auth/forgot-password")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(toJson(forgotPasswordForm)))
                .andDo(print())
                .andExpect(status().isOk());

        verify(userService, times(1)).forgotPassword(anyString());
    }

    @Test
    void deveResetPassword() throws Exception {
        String token = "token";
        NewPasswordForm newPasswordForm = new NewPasswordForm("senha");

        doNothing().when(userService).resetPassword(anyString(), any(NewPasswordForm.class));

        mockMvc
                .perform(
                        post("/auth/reset-password")
                                .contentType(MediaType.APPLICATION_JSON)
                                .param("token", token)
                                .content(toJson(newPasswordForm)))
                .andExpect(status().isOk());

        verify(userService, times(1)).resetPassword(anyString(), any(NewPasswordForm.class));
    }

    @Test
    void deveVerifyEmail() throws Exception {
        String token = "token";

        doNothing().when(userService).validateEmailUser(anyString());

        mockMvc
                .perform(
                        get("/auth/verify-email")
                                .contentType(MediaType.APPLICATION_JSON)
                                .param("token", token))
                .andExpect(status().isOk());

        verify(userService, times(1)).validateEmailUser(anyString());
    }

}
