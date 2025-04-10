package com.java.fiap.login.presentation;

import com.java.fiap.login.application.dto.UserLoginDTO;
import com.java.fiap.login.service.KeycloakAdminService;
import com.java.fiap.login.service.UserService;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.keycloak.KeycloakPrincipal;
import org.keycloak.KeycloakSecurityContext;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.representations.AccessToken;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthenticationController {

  private final KeycloakAdminService keycloakAdminService;
  private final UserService userService;

  @PostMapping(value = "/login")
  public ResponseEntity<Map<String, String>> login(@RequestBody UserLoginDTO login) {
    try {
      Keycloak keycloak = keycloakAdminService.getKeycloakUser(login);
      String token = keycloak.tokenManager().getAccessToken().getToken();

      return ResponseEntity.ok(Map.of("access_token", token));

    } catch (Exception e) {
      log.error("Error Token Keycloak: {}", e.getMessage(), e);
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
          .body(Map.of("error", "Username or password incorrect"));
    }
  }

  @GetMapping("/verify-email")
  public String verifyEmail(@RequestParam String token) throws IOException {
    userService.validateEmailUser(token);
    Path path = new ClassPathResource("page/email-validate-success.html").getFile().toPath();
    return Files.readString(path, StandardCharsets.UTF_8);
  }
}
