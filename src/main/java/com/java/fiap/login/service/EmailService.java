package com.java.fiap.login.service;

public interface EmailService {

  void sendEmail(String to, String subject, String body);
}
