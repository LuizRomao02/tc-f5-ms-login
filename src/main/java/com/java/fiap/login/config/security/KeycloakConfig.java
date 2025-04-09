package com.java.fiap.login.config.security;

import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import javax.net.ssl.SSLContext;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.ssl.SSLContextBuilder;
import org.jboss.resteasy.client.jaxrs.ResteasyClient;
import org.jboss.resteasy.client.jaxrs.ResteasyClientBuilder;
import org.jboss.resteasy.client.jaxrs.internal.ResteasyClientBuilderImpl;
import org.keycloak.OAuth2Constants;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.KeycloakBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@Slf4j
public class KeycloakConfig {

  @Value("${keycloak.auth-server-url}")
  private String keycloakServerUrl;

  @Value("${keycloak.realm}")
  private String keycloakRealm;

  @Value("${keycloak.resource}")
  private String keycloakClientId;

  @Value("${keycloak.credentials.secret}")
  private String keycloakClientSecret;

  @Value("${keycloak.disable-trust-manager:false}")
  private boolean disableTrustManager;

  @Bean
  public Keycloak keycloak() {
    ResteasyClientBuilderImpl builder = new ResteasyClientBuilderImpl();
    builder.connectionPoolSize(10);

    if (disableTrustManager) {
      builder
          .disableTrustManager()
          .hostnameVerification(ResteasyClientBuilder.HostnameVerificationPolicy.WILDCARD)
          .sslContext(createSslContext());
    }

    ResteasyClient resteasyClient = builder.build();

    return KeycloakBuilder.builder()
        .serverUrl(keycloakServerUrl)
        .realm(keycloakRealm)
        .clientId(keycloakClientId)
        .clientSecret(keycloakClientSecret)
        .grantType(OAuth2Constants.CLIENT_CREDENTIALS)
        .resteasyClient(resteasyClient)
        .build();
  }

  private SSLContext createSslContext() {
    try {
      return new SSLContextBuilder().loadTrustMaterial(null, (x509Certs, authType) -> true).build();
    } catch (NoSuchAlgorithmException | KeyStoreException | KeyManagementException e) {
      log.error("Failed to create custom SSL context for Keycloak", e);
      throw new RuntimeException("Failed to create SSL context", e);
    }
  }
}
