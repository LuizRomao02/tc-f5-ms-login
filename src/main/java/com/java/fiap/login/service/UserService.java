package com.java.fiap.login.service;

import com.java.fiap.login.application.dto.UserRegisteredEvent;
import com.java.fiap.login.application.dto.form.NewPasswordForm;

public interface UserService {

  void createUser(UserRegisteredEvent event);

  void validateEmailUser(String token);

  void forgotPassword(String email);

  void resetPassword(String token, NewPasswordForm newPasswordForm);
}
