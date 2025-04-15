package com.java.fiap.login.domain.model;

import com.java.fiap.login.application.dto.enums.UserTypeEnum;
import jakarta.persistence.*;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import lombok.*;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserLogin {

  @Id private String id;

  @Column(nullable = false, unique = true)
  private String userId;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private UserTypeEnum type;

  @Column(nullable = false)
  private boolean isEnabled;

  @Column(nullable = false)
  private String username;

  @Column(nullable = false)
  private String firstName;

  @Column(nullable = false)
  private String lastName;

  @Column(nullable = false)
  private String email;

  @Builder.Default
  private boolean emailVerified = false;

  private String tokenVerification;

  @Column(nullable = false)
  private LocalDateTime createdAt;

  private LocalDateTime updatedAt;

  @PrePersist
  public void prePersist() {
    createdAt = Instant.now().atZone(ZoneId.of("UTC")).toLocalDateTime();
    updatedAt = null;
  }

  @PreUpdate
  public void preUpdate() {
    updatedAt = Instant.now().atZone(ZoneId.of("UTC")).toLocalDateTime();
  }
}
