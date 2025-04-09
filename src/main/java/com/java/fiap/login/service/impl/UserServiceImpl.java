package com.java.fiap.login.service.impl;

import com.java.fiap.login.application.dto.UserKeycloak;
import com.java.fiap.login.application.dto.UserRegisteredEvent;
import com.java.fiap.login.domain.model.UserLogin;
import com.java.fiap.login.domain.repository.UserLoginRepository;
import com.java.fiap.login.service.KeycloakAdminService;
import com.java.fiap.login.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

  private final KeycloakAdminService keycloakService;
  private final UserLoginRepository loginRepository;

  @Transactional
  @Override
  public void createUser(UserRegisteredEvent event) {
    UserLogin userLogin =
        UserLogin.builder()
            .userId(event.getId())
            .isEnabled(true)
            .type(event.getType())
            .email(event.getEmail())
            .username(event.getType() + "_" + event.getId())
            .build();

    UserKeycloak userKeycloak = keycloakService.createKeycloakUser(userLogin, event.getPassword());

    userLogin.setId(userKeycloak.getId());
    loginRepository.save(userLogin);

    keycloakService.addRoleToUser(userLogin);
  }
}
