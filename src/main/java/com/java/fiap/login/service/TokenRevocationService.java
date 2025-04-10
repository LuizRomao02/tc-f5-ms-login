package com.java.fiap.login.service;

import com.java.fiap.login.application.dto.InfoUserKeycloakDTO;
import org.keycloak.representations.AccessToken;

public interface TokenRevocationService {

  void revokeToken(InfoUserKeycloakDTO userKeycloak, AccessToken accessToken);
}
