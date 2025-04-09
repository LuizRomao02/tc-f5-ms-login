package com.java.fiap.login.config.security;

import java.util.*;
import org.springframework.core.convert.converter.Converter;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.stereotype.Component;

@Component
public class JwtAuthConverter implements Converter<Jwt, AbstractAuthenticationToken> {

  private static final String ROLES_CLAIM = "roles";
  private static final String REALM_ACCESS_CLAIM = "realm_access";
  private static final String RESOURCE_ACCESS_CLAIM = "resource_access";
  private static final String ROLE_PREFIX = "ROLE_";

  private final JwtGrantedAuthoritiesConverter jwtGrantedAuthoritiesConverter =
      new JwtGrantedAuthoritiesConverter();

  @Override
  public AbstractAuthenticationToken convert(@NonNull Jwt jwt) {
    Collection<GrantedAuthority> authorities = extractAuthorities(jwt);
    return new JwtAuthenticationToken(jwt, authorities, jwt.getSubject());
  }

  private Collection<GrantedAuthority> extractAuthorities(Jwt jwt) {
    Set<GrantedAuthority> authorities = new HashSet<>(jwtGrantedAuthoritiesConverter.convert(jwt));
    extractRealmAccessRoles(jwt, authorities);
    extractResourceAccessRoles(jwt, authorities);
    return authorities;
  }

  private void extractRealmAccessRoles(Jwt jwt, Set<GrantedAuthority> authorities) {
    Map<String, Object> realmAccess = jwt.getClaimAsMap(REALM_ACCESS_CLAIM);
    if (realmAccess != null && realmAccess.containsKey(ROLES_CLAIM)) {
      Collection<String> roles = safeCastToCollection(realmAccess.get(ROLES_CLAIM));
      roles.forEach(role -> authorities.add(new SimpleGrantedAuthority(ROLE_PREFIX + role)));
    }
  }

  private void extractResourceAccessRoles(Jwt jwt, Set<GrantedAuthority> authorities) {
    Map<String, Object> resourceAccess = jwt.getClaimAsMap(RESOURCE_ACCESS_CLAIM);
    if (resourceAccess != null) {
      resourceAccess.values().stream()
          .filter(Map.class::isInstance)
          .map(resource -> (Map<String, Object>) resource)
          .filter(resource -> resource.containsKey(ROLES_CLAIM))
          .map(resource -> safeCastToCollection(resource.get(ROLES_CLAIM)))
          .flatMap(Collection::stream)
          .map(SimpleGrantedAuthority::new)
          .forEach(authorities::add);
    }
  }

  @SuppressWarnings("unchecked")
  private Collection<String> safeCastToCollection(Object rolesObj) {
    if (rolesObj instanceof Collection) {
      return (Collection<String>) rolesObj;
    }
    return Collections.emptyList();
  }
}
