package com.java.fiap.login.presentation;

import com.java.fiap.login.application.dto.UserRegisteredEvent;
import com.java.fiap.login.application.dto.enums.UserTypeEnum;
import com.java.fiap.login.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/teste")
@RequiredArgsConstructor
public class TestClass {

  private final UserService userService;

  @PostMapping
  public String teste() {
    userService.createUser(
        UserRegisteredEvent.builder()
            .email("luiz@email.com")
            .id("id-teste-teste")
            .firstName("Luiz")
            .lastName("Romão")
            .password("123456")
            .email("lh168205@gmail.com")
            .type(UserTypeEnum.DOCTOR)
            .name("Luiz")
            .build());
    return "ok";
  }
}
