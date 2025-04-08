package com.java.fiap.login.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

  public static final String EXCHANGE = "user.exchange";
  public static final String ROUTING_KEY = "user.registered";
  public static final String QUEUE = "user.registered.queue";

  @Bean
  public DirectExchange exchange() {
    return new DirectExchange(EXCHANGE);
  }

  @Bean
  public Queue queue() {
    return new Queue(QUEUE, true);
  }

  @Bean
  public Binding binding(Queue queue, DirectExchange exchange) {
    return BindingBuilder.bind(queue).to(exchange).with(ROUTING_KEY);
  }
}
