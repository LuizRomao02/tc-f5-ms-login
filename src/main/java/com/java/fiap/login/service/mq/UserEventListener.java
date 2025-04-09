package com.java.fiap.login.service.mq;

import com.java.fiap.login.application.dto.UserRegisteredEvent;
import com.java.fiap.login.config.RabbitMQConfig;
import com.java.fiap.login.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserEventListener {

  private final UserService userService;

  @RabbitListener(queues = RabbitMQConfig.QUEUE)
  public void handleUserRegistered(UserRegisteredEvent event) {
    log.info("UserRegistered event received: {}", event);
    userService.createUser(event);
  }
}
