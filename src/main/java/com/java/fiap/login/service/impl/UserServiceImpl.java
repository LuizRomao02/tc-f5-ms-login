package com.java.fiap.login.service.impl;

import com.java.fiap.login.application.dto.UserKeycloak;
import com.java.fiap.login.application.dto.UserRegisteredEvent;
import com.java.fiap.login.domain.model.UserLogin;
import com.java.fiap.login.domain.repository.UserLoginRepository;
import com.java.fiap.login.service.EmailService;
import com.java.fiap.login.service.KeycloakAdminService;
import com.java.fiap.login.service.UserService;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

  @Value("${url-email}")
  private String url;

  private final KeycloakAdminService keycloakService;
  private final UserLoginRepository loginRepository;
  private final EmailService emailService;

  @Transactional
  @Override
  public void createUser(UserRegisteredEvent event) {
    String username = event.getType() + "_" + event.getId();

    UserLogin userLogin =
        UserLogin.builder()
            .userId(event.getId())
            .isEnabled(true)
            .firstName(event.getFirstName())
            .lastName(event.getLastName())
            .type(event.getType())
            .email(event.getEmail())
            .username(username.toLowerCase())
            .build();

    UserKeycloak userKeycloak = keycloakService.createKeycloakUser(userLogin, event.getPassword());

    userLogin.setId(userKeycloak.getId());
    userLogin.setEmailVerified(false);
    userLogin.setTokenVerification(UUID.randomUUID().toString());

    loginRepository.save(userLogin);
    keycloakService.addRoleToUser(userLogin);

    sendNotificationNewAccount(userLogin);
  }

  @Override
  @Transactional
  public void validateEmailUser(String token) {
    UserLogin userLogin = loginRepository.findByTokenVerification(token);

    if (userLogin != null) {
      userLogin.setEmailVerified(true);
      userLogin.setTokenVerification(null);
      keycloakService.confirmEmail(userLogin);
    } else {
      throw new RuntimeException("Invalid token");
    }
  }

  private void sendNotificationNewAccount(UserLogin userLogin) {
    String link = url + "/auth/verify-email?token=" + userLogin.getTokenVerification();

    Map<String, String> values = new HashMap<>();
    values.put("name", userLogin.getFirstName() + " " + userLogin.getLastName());
    values.put("username", userLogin.getUsername());
    values.put("loginType", userLogin.getType().name());
    values.put("verifyEmailLink", link);

    String html = loadTemplate(values);

    emailService.sendEmail(userLogin.getEmail(), "Bem-vindo ao sistema!", html);
  }

  private String loadTemplate(Map<String, String> values) {
    try {
      Path path =
          new ClassPathResource("template/template-email-new-account.html").getFile().toPath();
      String content = Files.readString(path, StandardCharsets.UTF_8);

      for (Map.Entry<String, String> entry : values.entrySet()) {
        content = content.replace("{{" + entry.getKey() + "}}", entry.getValue());
      }

      return content;
    } catch (IOException e) {
      throw new RuntimeException("Error loading email template", e);
    }
  }
}
