package com.java.fiap.login.service.cache;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Service;

@Service
public class UserTokenRevocationMemoryCache {

  private final Set<String> revokedTokens = ConcurrentHashMap.newKeySet();

  public void revokeToken(String tokenId) {
    revokedTokens.add(tokenId);
  }

  public boolean isTokenRevoked(String tokenId) {
    return revokedTokens.contains(tokenId);
  }
}
