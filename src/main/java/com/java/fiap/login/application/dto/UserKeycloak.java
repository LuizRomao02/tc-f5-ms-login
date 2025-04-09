package com.java.fiap.login.application.dto;

import java.io.Serializable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserKeycloak implements Serializable {

  private String id;
  private String username;
  private String email;
}
