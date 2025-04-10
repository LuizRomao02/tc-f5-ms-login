package com.java.fiap.login.domain.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import lombok.*;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserTokenRevoked {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private String id;

  @Column(nullable = false, unique = true)
  private String tokenId;

  @Column(nullable = false)
  private String userId;

  @Column(nullable = false)
  private LocalDateTime expirationDateTime;
}
