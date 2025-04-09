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
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

  private final KeycloakAdminService keycloakService;
  private final UserLoginRepository loginRepository;
  private final EmailService emailService;

  @Transactional
  @Override
  public void createUser(UserRegisteredEvent event) {
    UserLogin userLogin =
        UserLogin.builder()
            .userId(event.getId())
            .isEnabled(true)
            .fullName(event.getFullName())
            .type(event.getType())
            .email(event.getEmail())
            .username(event.getType() + "_" + event.getId())
            .build();

    UserKeycloak userKeycloak = keycloakService.createKeycloakUser(userLogin, event.getPassword());

    userLogin.setId(userKeycloak.getId());
    loginRepository.save(userLogin);
    keycloakService.addRoleToUser(userLogin);
    sendNotificationNewAccount(userLogin);
  }

  private void sendNotificationNewAccount(UserLogin userLogin) {
    Map<String, String> values = new HashMap<>();
    values.put("name", userLogin.getFullName());
    values.put("username", userLogin.getUsername());
    values.put("loginType", userLogin.getType().name());

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
