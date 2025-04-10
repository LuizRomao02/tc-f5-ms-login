package com.java.fiap.login.presentation;

import com.java.fiap.login.application.dto.UserLoginDTO;
import com.java.fiap.login.service.KeycloakAdminService;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthenticationController {

  private final KeycloakAdminService keycloakAdminService;

  @PostMapping(value = "/login")
  public ResponseEntity<Map<String, String>> login(@RequestBody UserLoginDTO login) {
    return ResponseEntity.ok()
        .body(
            Map.of(
                "access_token",
                keycloakAdminService
                    .getKeycloakUser(login)
                    .tokenManager()
                    .getAccessToken()
                    .getToken()));
  }
}
