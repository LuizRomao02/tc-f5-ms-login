package com.java.fiap.login.config.security;

import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
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

  private static final String ROLES = "roles";

  private final JwtGrantedAuthoritiesConverter jwtGrantedAuthoritiesConverter;

  public JwtAuthConverter() {
    this.jwtGrantedAuthoritiesConverter = new JwtGrantedAuthoritiesConverter();
  }

  @Override
  public AbstractAuthenticationToken convert(@NonNull Jwt jwt) {
    Collection<GrantedAuthority> authorities = extractAuthorities(jwt);
    return new JwtAuthenticationToken(jwt, authorities, jwt.getSubject());
  }

  private Collection<GrantedAuthority> extractAuthorities(Jwt jwt) {
    Set<GrantedAuthority> authorities = new HashSet<>(jwtGrantedAuthoritiesConverter.convert(jwt));

    extractRealmAccessRoles(jwt, authorities);

    Map<String, Object> resourceAccess = jwt.getClaimAsMap("resource_access");
    if (resourceAccess != null) {
      authorities.addAll(extractResourceAccessRoles(resourceAccess));
    }

    return authorities;
  }

  private void extractRealmAccessRoles(Jwt jwt, Set<GrantedAuthority> authorities) {
    Map<String, Object> realmAccess = jwt.getClaimAsMap("realm_access");
    if (realmAccess != null && realmAccess.containsKey(ROLES)) {
      Object rolesObj = realmAccess.get(ROLES);

      if (rolesObj instanceof Collection<?>) {
        @SuppressWarnings("unchecked")
        Collection<String> roles = (Collection<String>) rolesObj;
        roles.forEach(role -> authorities.add(new SimpleGrantedAuthority("ROLE_" + role)));
      }
    }
  }

  private Collection<GrantedAuthority> extractResourceAccessRoles(
      Map<String, Object> resourceAccess) {
    Set<GrantedAuthority> authorities = new HashSet<>();

    for (Map.Entry<String, Object> entry : resourceAccess.entrySet()) {
      Object resource = entry.getValue();
      if (resource instanceof Map) {
        @SuppressWarnings("unchecked")
        Map<String, Object> resourceDetails = (Map<String, Object>) resource;

        if (resourceDetails.containsKey(ROLES)) {
          Object rolesObj = resourceDetails.get(ROLES);
          if (rolesObj instanceof Collection<?>) {
            @SuppressWarnings("unchecked")
            Collection<String> roles = (Collection<String>) rolesObj;

            authorities.addAll(
                roles.stream().map(SimpleGrantedAuthority::new).collect(Collectors.toSet()));
          }
        }
      }
    }

    return authorities;
  }
}
