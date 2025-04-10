package com.java.fiap.login.service.impl;

import com.java.fiap.login.application.dto.UserKeycloak;
import com.java.fiap.login.application.dto.UserLoginDTO;
import com.java.fiap.login.application.dto.enums.UserTypeEnum;
import com.java.fiap.login.domain.model.UserLogin;
import com.java.fiap.login.service.KeycloakAdminService;
import jakarta.ws.rs.InternalServerErrorException;
import jakarta.ws.rs.client.Client;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import javax.net.ssl.SSLContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.ssl.SSLContextBuilder;
import org.jboss.resteasy.client.jaxrs.ResteasyClientBuilder;
import org.jboss.resteasy.client.jaxrs.internal.ResteasyClientBuilderImpl;
import org.keycloak.OAuth2Constants;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.KeycloakBuilder;
import org.keycloak.admin.client.resource.RealmResource;
import org.keycloak.admin.client.resource.UserResource;
import org.keycloak.admin.client.resource.UsersResource;
import org.keycloak.representations.idm.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class KeycloakAdminServiceImpl implements KeycloakAdminService {

  private final Keycloak keycloak;

  @Value("${keycloak.auth-server-url}")
  private String keycloakServerUrl;

  @Value("${keycloak.realm}")
  private String keycloakRealm;

  @Value("${keycloak.resource}")
  private String keycloakClientId;

  @Value("${keycloak.credentials.secret}")
  private String keycloakClientSecret;

  // ------------------------------------
  // Public Methods
  // ------------------------------------

  @Override
  public UserKeycloak createKeycloakUser(UserLogin userLogin, String password) {
    UserRepresentation user = buildUserRepresentation(userLogin, password);
    getUsersResource().create(user);
    return getUserKeycloakByUsername(userLogin.getUsername());
  }

  @Override
  public void addRoleToUser(UserLogin userLogin) {
    String roleName = getRoleNameByUserType(userLogin.getType());
    assignRoleToUser(userLogin.getId(), roleName, keycloakClientId);
  }

  @Override
  public Keycloak getKeycloakUser(UserLoginDTO userLogin) {
    ResteasyClientBuilder resteasyClientBuilder =
        new ResteasyClientBuilderImpl().connectionPoolSize(10);

    resteasyClientBuilder
        .connectionPoolSize(10)
        .disableTrustManager()
        .hostnameVerification(ResteasyClientBuilder.HostnameVerificationPolicy.WILDCARD)
        .sslContext(createSslContext());

    Client client = resteasyClientBuilder.build();

    return KeycloakBuilder.builder()
        .serverUrl(keycloakServerUrl)
        .realm(keycloakRealm)
        .clientId(keycloakClientId)
        .clientSecret(keycloakClientSecret)
        .resteasyClient(client)
        .username(userLogin.getUsername())
        .password(userLogin.getPassword())
        .grantType(OAuth2Constants.CLIENT_CREDENTIALS)
        .build();
  }

  // ------------------------------------
  // Private Helpers
  // ------------------------------------

  private SSLContext createSslContext() {
    try {
      return new SSLContextBuilder()
          .loadTrustMaterial(null, (x509Certificates, authType) -> true)
          .build();
    } catch (NoSuchAlgorithmException | KeyStoreException | KeyManagementException e) {
      log.error("Error creating SSL context: {}", e.getMessage(), e);
      throw new InternalServerErrorException(e);
    }
  }

  private void assignRoleToUser(String userId, String roleName, String client) {
    RoleRepresentation role = fetchClientRole(roleName, client);
    UserResource userResource = getUserResource(userId);

    boolean alreadyAssigned =
        userResource.roles().clientLevel(getClientInternalId(client)).listAll().stream()
            .anyMatch(roleRep -> roleRep.getName().equals(roleName));

    if (!alreadyAssigned) {
      userResource
          .roles()
          .clientLevel(getClientInternalId(client))
          .add(Collections.singletonList(role));
    }
  }

  private UserRepresentation buildUserRepresentation(UserLogin userLogin, String password) {
    UserRepresentation user = new UserRepresentation();
    user.setUsername(userLogin.getUsername());
    user.setEmail(userLogin.getEmail());
    user.setEnabled(true);
    user.setAttributes(buildUserAttributes(userLogin));
    user.setCredentials(List.of(buildPasswordCredential(password)));
    return user;
  }

  private Map<String, List<String>> buildUserAttributes(UserLogin userLogin) {
    Map<String, List<String>> attributes = new HashMap<>();
    attributes.put("userType", List.of(userLogin.getType().name()));
    attributes.put("userId", List.of(userLogin.getUserId()));
    return attributes;
  }

  private CredentialRepresentation buildPasswordCredential(String password) {
    CredentialRepresentation credential = new CredentialRepresentation();
    credential.setType(CredentialRepresentation.PASSWORD);
    credential.setValue(password);
    credential.setTemporary(false);
    return credential;
  }

  private String getRoleNameByUserType(UserTypeEnum type) {
    return switch (type) {
      case DOCTOR -> "ROLE_DOCTOR";
      case ADMIN -> "ROLE_ADMIN";
      default -> "ROLE_PATIENT";
    };
  }

  private UserKeycloak getUserKeycloakByUsername(String username) {
    return getRealmResource().users().search(username, true).stream()
        .findFirst()
        .map(this::toUserKeycloak)
        .orElseThrow(() -> new RuntimeException("Usuário não encontrado: " + username));
  }

  private UserKeycloak toUserKeycloak(UserRepresentation user) {
    return UserKeycloak.builder()
        .id(user.getId())
        .username(user.getUsername())
        .email(user.getEmail())
        .build();
  }

  private RoleRepresentation fetchClientRole(String roleName, String client) {
    try {
      return getRealmResource()
          .clients()
          .get(getClientInternalId(client))
          .roles()
          .get(roleName)
          .toRepresentation();
    } catch (Exception e) {
      String message = String.format("Role '%s' not found in client '%s'.", roleName, client);
      log.error(message, e);
      throw new InternalServerErrorException(message);
    }
  }

  private String getClientInternalId(String client) {
    return getRealmResource().clients().findByClientId(client.toLowerCase()).stream()
        .findFirst()
        .map(ClientRepresentation::getId)
        .orElseThrow(() -> new InternalServerErrorException("Client not found: " + client));
  }

  private UserResource getUserResource(String userId) {
    try {
      return getRealmResource().users().get(userId);
    } catch (Exception e) {
      log.error("Usuário {} não encontrado no Keycloak.", userId, e);
      throw new InternalServerErrorException("Usuário não encontrado no Keycloak: " + userId);
    }
  }

  private UsersResource getUsersResource() {
    return getRealmResource().users();
  }

  private RealmResource getRealmResource() {
    return keycloak.realm(keycloakRealm);
  }
}
