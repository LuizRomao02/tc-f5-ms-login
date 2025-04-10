package com.java.fiap.login.service.impl;

import com.java.fiap.login.application.dto.InfoUserKeycloakDTO;
import com.java.fiap.login.domain.model.UserTokenRevoked;
import com.java.fiap.login.domain.repository.UserTokenRevokedRepository;
import com.java.fiap.login.service.TokenRevocationService;
import com.java.fiap.login.service.cache.UserTokenRevocationMemoryCache;
import jakarta.transaction.Transactional;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import lombok.RequiredArgsConstructor;
import org.keycloak.representations.AccessToken;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TokenRevocationServiceImpl implements TokenRevocationService {

  private final UserTokenRevokedRepository userTokenRevokedRepository;
  private final UserTokenRevocationMemoryCache userTokenRevocationMemoryCache;

  @Override
  @Transactional
  public void revokeToken(InfoUserKeycloakDTO userKeycloak, AccessToken accessToken) {
    long expMilliseconds = accessToken.getExp() * 1000;
    Instant instant = Instant.ofEpochMilli(expMilliseconds);
    LocalDateTime expirationDate = instant.atZone(ZoneId.of("UTC")).toLocalDateTime();

    UserTokenRevoked revoked =
        UserTokenRevoked.builder()
            .tokenId(accessToken.getId())
            .userId(userKeycloak.getUserId())
            .expirationDateTime(expirationDate)
            .build();

    userTokenRevokedRepository.save(revoked);
    userTokenRevocationMemoryCache.revokeToken(accessToken.getId());
  }
}
