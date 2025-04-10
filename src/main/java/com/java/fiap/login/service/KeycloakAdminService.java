package com.java.fiap.login.service;

import com.java.fiap.login.application.dto.UserKeycloak;
import com.java.fiap.login.application.dto.UserLoginDTO;
import com.java.fiap.login.domain.model.UserLogin;
import org.keycloak.admin.client.Keycloak;

public interface KeycloakAdminService {

  UserKeycloak createKeycloakUser(UserLogin userLogin, String password);

  void addRoleToUser(UserLogin userLogin);

  Keycloak getKeycloakUser(UserLoginDTO userLogin);
}
