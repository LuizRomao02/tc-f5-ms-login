package com.java.fiap.login.presentation;

import com.java.fiap.login.application.dto.InfoUserKeycloakDTO;
import com.java.fiap.login.application.dto.UserLoginDTO;
import com.java.fiap.login.application.dto.form.ForgotPasswordForm;
import com.java.fiap.login.application.dto.form.NewPasswordForm;
import com.java.fiap.login.service.KeycloakAdminService;
import com.java.fiap.login.service.UserService;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.keycloak.admin.client.Keycloak;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
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

  @PostMapping(value = "/logout")
  public ResponseEntity<Void> logout(@ModelAttribute final InfoUserKeycloakDTO userKeycloak) {
    keycloakAdminService.logout(userKeycloak);
    return ResponseEntity.ok().build();
  }

  @PutMapping(value = "/forgot-password", consumes = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<Void> forgotPassword(@RequestBody ForgotPasswordForm forgotPasswordForm) {
    userService.forgotPassword(forgotPasswordForm.email());
    return ResponseEntity.ok().build();
  }

  @PostMapping(value = "/reset-password", consumes = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<Void> resetPassword(
      @RequestParam String token, @RequestBody NewPasswordForm newPasswordForm) {
    userService.resetPassword(token, newPasswordForm);
    return ResponseEntity.ok().build();
  }

  @GetMapping("/verify-email")
  public String verifyEmail(@RequestParam String token) throws IOException {
    userService.validateEmailUser(token);
    Path path = new ClassPathResource("page/email-validate-success.html").getFile().toPath();
    return Files.readString(path, StandardCharsets.UTF_8);
  }
}
