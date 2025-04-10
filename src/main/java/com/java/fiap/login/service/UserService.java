package com.java.fiap.login.service;

import com.java.fiap.login.application.dto.UserRegisteredEvent;

public interface UserService {

  void createUser(UserRegisteredEvent event);

  void validateEmailUser(String token);
}
