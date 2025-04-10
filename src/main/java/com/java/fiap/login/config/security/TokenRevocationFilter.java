package com.java.fiap.login.config.security;

import com.java.fiap.login.service.cache.UserTokenRevocationMemoryCache;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

import lombok.RequiredArgsConstructor;
import org.keycloak.KeycloakPrincipal;
import org.keycloak.KeycloakSecurityContext;
import org.keycloak.adapters.springsecurity.token.KeycloakAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
@RequiredArgsConstructor
public class TokenRevocationFilter extends OncePerRequestFilter {

  private final UserTokenRevocationMemoryCache userTokenRevocationMemoryCache;

  @Override
  protected void doFilterInternal(
      HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
      throws ServletException, IOException {

    var auth = SecurityContextHolder.getContext().getAuthentication();

    if (auth instanceof KeycloakAuthenticationToken keycloakAuth) {
      KeycloakPrincipal<?> principal = (KeycloakPrincipal<?>) keycloakAuth.getPrincipal();
      KeycloakSecurityContext context = principal.getKeycloakSecurityContext();

      String tokenId = context.getToken().getId(); // <-- jti

      if (userTokenRevocationMemoryCache.isTokenRevoked(tokenId)) {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.getWriter().write("Token has been revoked");
        return;
      }
    }

    filterChain.doFilter(request, response);
  }
}
