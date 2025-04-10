package com.java.fiap.login.application.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.usertype.UserType;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InfoUserKeycloakDTO {

  public String userId;
  public UserType userType;
}
