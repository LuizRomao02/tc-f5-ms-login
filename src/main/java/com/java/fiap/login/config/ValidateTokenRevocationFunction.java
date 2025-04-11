package com.java.fiap.login.config;

import com.java.fiap.login.application.dto.TokenValidationRequest;
import com.java.fiap.login.service.cache.UserTokenRevocationMemoryCache;
import java.util.function.Function;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component("validateTokenRevocation")
@RequiredArgsConstructor
public class ValidateTokenRevocationFunction implements Function<TokenValidationRequest, Boolean> {

  private final UserTokenRevocationMemoryCache cache;

  @Override
  public Boolean apply(TokenValidationRequest tokenValidationRequest) {
    return !cache.isTokenRevoked(tokenValidationRequest.getJti());
  }
}
