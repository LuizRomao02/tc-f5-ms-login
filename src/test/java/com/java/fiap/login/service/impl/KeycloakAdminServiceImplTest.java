package com.java.fiap.login.service.impl;

import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;

import com.java.fiap.login.application.dto.InfoUserKeycloakDTO;
import com.java.fiap.login.application.dto.UserLoginDTO;
import com.java.fiap.login.domain.model.UserLogin;
import com.java.fiap.login.service.KeycloakAdminService;
import com.java.fiap.login.service.TokenRevocationService;
import com.java.fiap.login.utils.UserHelper;
import jakarta.ws.rs.InternalServerErrorException;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.keycloak.KeycloakPrincipal;
import org.keycloak.KeycloakSecurityContext;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.resource.ClientResource;
import org.keycloak.admin.client.resource.ClientsResource;
import org.keycloak.admin.client.resource.RealmResource;
import org.keycloak.admin.client.resource.RoleMappingResource;
import org.keycloak.admin.client.resource.RoleResource;
import org.keycloak.admin.client.resource.RoleScopeResource;
import org.keycloak.admin.client.resource.RolesResource;
import org.keycloak.admin.client.resource.UserResource;
import org.keycloak.admin.client.resource.UsersResource;
import org.keycloak.representations.AccessToken;
import org.keycloak.representations.idm.ClientRepresentation;
import org.keycloak.representations.idm.RoleRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.util.ReflectionTestUtils;

class KeycloakAdminServiceImplTest {

    AutoCloseable openMocks;
    private KeycloakAdminService keycloakAdminService;
    @Mock
    private Keycloak keycloak;
    @Mock private TokenRevocationService tokenRevocationService;

    private final RealmResource realmResource = mock(RealmResource.class);
    private final UsersResource usersResource = mock(UsersResource.class);
    private final UserResource userResource = mock(UserResource.class);
    private final UserRepresentation userRepresentation = mock(UserRepresentation.class);
    private final ClientsResource clientsResource = mock(ClientsResource.class);
    private final ClientRepresentation clientRepresentation = mock(ClientRepresentation.class);
    private final ClientResource clientResource = mock(ClientResource.class);
    private final RolesResource rolesResource = mock(RolesResource.class);
    private final RoleResource roleResource = mock(RoleResource.class);
    private final RoleRepresentation roleRepresentation = mock(RoleRepresentation.class);
    private final RoleMappingResource roleMappingResource = mock(RoleMappingResource.class);
    private final RoleScopeResource roleScopeResource = mock(RoleScopeResource.class);

    @BeforeEach
    void setUp() {
        openMocks = MockitoAnnotations.openMocks(this);
        var keycloakAdminServiceImpl = new KeycloakAdminServiceImpl(keycloak, tokenRevocationService);
        ReflectionTestUtils.setField(keycloakAdminServiceImpl, "keycloakServerUrl", "http://localhost:8080");
        ReflectionTestUtils.setField(keycloakAdminServiceImpl, "keycloakRealm", "my-realm");
        ReflectionTestUtils.setField(keycloakAdminServiceImpl, "keycloakClientId", "my-client-id");
        ReflectionTestUtils.setField(keycloakAdminServiceImpl, "keycloakClientSecret", "my-secret");
        this.keycloakAdminService = keycloakAdminServiceImpl;
    }

    @AfterEach
    void tearDown() throws Exception {
        openMocks.close();
    }

    @Test
    void deveCriarKeycloakUser() {
        UserLogin userLogin = UserHelper.gerarUserLogin();

        UserRepresentation userRepresentation = mock(UserRepresentation.class);

        when(keycloak.realm(anyString())).thenReturn(this.realmResource);
        when(this.realmResource.users()).thenReturn(this.usersResource);
        when(this.usersResource.search(anyString(), anyBoolean())).thenReturn(List.of(userRepresentation));

        assertThatNoException().isThrownBy(() -> keycloakAdminService.createKeycloakUser(userLogin, UserHelper.PASSWORD));
    }

    @Test
    void deveAddRoleToUser() {
        UserLogin userLogin = UserHelper.gerarUserLogin();

        when(keycloak.realm(anyString())).thenReturn(this.realmResource);
        when(this.realmResource.users()).thenReturn(this.usersResource);
        when(this.realmResource.clients()).thenReturn(this.clientsResource);
        when(this.usersResource.get(anyString())).thenReturn(this.userResource);
        when(this.clientsResource.findByClientId(anyString())).thenReturn(new ArrayList<>(List.of(this.clientRepresentation)));
        when(this.clientsResource.get(anyString())).thenReturn(this.clientResource);
        when(this.clientRepresentation.getId()).thenReturn("my-client-id");
        when(this.clientResource.roles()).thenReturn(this.rolesResource);
        when(this.rolesResource.get(anyString())).thenReturn(this.roleResource);
        when(this.roleResource.toRepresentation()).thenReturn(this.roleRepresentation);
        when(this.userResource.roles()).thenReturn(this.roleMappingResource);
        when(this.roleMappingResource.clientLevel(anyString())).thenReturn(this.roleScopeResource);

        assertThatNoException().isThrownBy(() -> keycloakAdminService.addRoleToUser(userLogin));
    }

    @Test
    void deveGerarErroQuandoAddRoleToUserSemUser() {
        UserLogin userLogin = UserHelper.gerarUserLogin();

        when(keycloak.realm(anyString())).thenReturn(this.realmResource);
        when(this.realmResource.users()).thenReturn(null);
        when(this.realmResource.clients()).thenReturn(this.clientsResource);
        when(this.clientsResource.findByClientId(anyString())).thenReturn(new ArrayList<>(List.of(this.clientRepresentation)));
        when(this.clientsResource.get(anyString())).thenReturn(this.clientResource);
        when(this.clientRepresentation.getId()).thenReturn("my-client-id");
        when(this.clientResource.roles()).thenReturn(this.rolesResource);
        when(this.rolesResource.get(anyString())).thenReturn(this.roleResource);
        when(this.roleResource.toRepresentation()).thenReturn(this.roleRepresentation);

        assertThatThrownBy(() -> keycloakAdminService.addRoleToUser(userLogin))
                .isInstanceOf(InternalServerErrorException.class)
                .hasMessage("Usuário não encontrado no Keycloak: %s".formatted(userLogin.getId()));
    }

    @Test
    void deveGerarErroQuandoAddRoleToUserClienteNaoEncontrado() {
        UserLogin userLogin = UserHelper.gerarUserLogin();

        when(keycloak.realm(anyString())).thenReturn(this.realmResource);
        when(this.realmResource.users()).thenReturn(this.usersResource);
        when(this.realmResource.clients()).thenReturn(this.clientsResource);

        assertThatThrownBy(() -> keycloakAdminService.addRoleToUser(userLogin))
            .isInstanceOf(InternalServerErrorException.class)
            .hasMessage("Role 'ROLE_PATIENT' not found in client 'my-client-id'.");
    }

    @Test
    void deveGetKeycloakUser() {
        UserLoginDTO userLoginDTO = UserHelper.gerarUserLoginDTO();

        assertThatNoException().isThrownBy(() -> keycloakAdminService.getKeycloakUser(userLoginDTO));
    }

    @Test
    void deveGerarErroQuandoGetKeycloakUserSemUsername() {
        UserLoginDTO userLoginDTO = UserHelper.gerarUserLoginDTO();
        userLoginDTO.setUsername(null);

        assertThatThrownBy(() -> keycloakAdminService.getKeycloakUser(userLoginDTO))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("username required");
    }

    @Test
    void deveConfirmEmail() {
        UserLogin userLogin = UserHelper.gerarUserLogin();

        when(keycloak.realm(anyString())).thenReturn(this.realmResource);
        when(this.realmResource.users()).thenReturn(this.usersResource);
        when(this.usersResource.get(anyString())).thenReturn(this.userResource);
        when(this.userResource.toRepresentation()).thenReturn(this.userRepresentation);

        assertThatNoException().isThrownBy(() -> keycloakAdminService.confirmEmail(userLogin));
    }

    @Test
    void deveGerarErroQuandoConfirmEmailComUserIdNull() {

        UserLogin userLogin = UserHelper.gerarUserLogin();
        userLogin.setUserId(null);

        assertThatThrownBy(() -> keycloakAdminService.confirmEmail(userLogin))
                .isInstanceOf(InternalServerErrorException.class)
                .hasMessage("Failed to confirm email for user: %s".formatted(userLogin.getUsername()));
    }

    @Test
    @SuppressWarnings("unchecked")
    void deveLogout() {
        InfoUserKeycloakDTO infoUserKeycloakDTO = UserHelper.gerarInfoUserKeycloakDTO();

        Authentication authentication = mock(Authentication.class);
        SecurityContext securityContext = mock(SecurityContext.class);
        KeycloakPrincipal<KeycloakSecurityContext> keycloakPrincipal = mock(KeycloakPrincipal.class);
        KeycloakSecurityContext keycloakSecurityContext = mock(KeycloakSecurityContext.class);
        AccessToken accessToken = mock(AccessToken.class);
        UserResource userResource = mock(UserResource.class);

        when(keycloak.realm(anyString())).thenReturn(this.realmResource);
        when(this.realmResource.users()).thenReturn(this.usersResource);
        when(this.usersResource.get(anyString())).thenReturn(this.userResource);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(keycloakPrincipal);
        when(keycloakPrincipal.getKeycloakSecurityContext()).thenReturn(keycloakSecurityContext);
        when(keycloakSecurityContext.getToken()).thenReturn(accessToken);
        when(keycloakAdminService.getUserResource(anyString())).thenReturn(userResource);
        doNothing().when(tokenRevocationService).revokeToken(any(InfoUserKeycloakDTO.class), any(AccessToken.class));

        SecurityContextHolder.setContext(securityContext);

        assertThatNoException().isThrownBy(() -> keycloakAdminService.logout(infoUserKeycloakDTO));
    }

    @Test
    void deveLogarErroDeLogout() {
        InfoUserKeycloakDTO infoUserKeycloakDTO = UserHelper.gerarInfoUserKeycloakDTO();

        assertThatNoException().isThrownBy(() -> keycloakAdminService.logout(infoUserKeycloakDTO));
    }

    @Test
    void deveResetPassword() {
        UserLogin userLogin = UserHelper.gerarUserLogin();

        when(keycloak.realm(anyString())).thenReturn(this.realmResource);
        when(this.realmResource.users()).thenReturn(this.usersResource);
        when(this.usersResource.get(anyString())).thenReturn(this.userResource);

        assertThatNoException().isThrownBy(() -> keycloakAdminService.resetPassword(userLogin, UserHelper.PASSWORD));
    }
}

