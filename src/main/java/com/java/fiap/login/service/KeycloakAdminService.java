package com.java.fiap.login.service;

import com.java.fiap.login.application.dto.UserKeycloak;
import com.java.fiap.login.application.dto.UserLoginDTO;
import com.java.fiap.login.domain.model.UserLogin;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.resource.UserResource;

public interface KeycloakAdminService {

  UserKeycloak createKeycloakUser(UserLogin userLogin, String password);

  void addRoleToUser(UserLogin userLogin);

  Keycloak getKeycloakUser(UserLoginDTO userLogin);

  UserResource getUserResource(String userId);

  void confirmEmail(UserLogin userLogin);
}
