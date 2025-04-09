package com.java.fiap.login.application.dto;

import com.java.fiap.login.application.dto.enums.UserTypeEnum;
import java.io.Serial;
import java.io.Serializable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserRegisteredEvent implements Serializable {

  @Serial private static final long serialVersionUID = 1L;

  private String id;
  private String name;
  private String email;
  private String password;
  private UserTypeEnum type;
}
