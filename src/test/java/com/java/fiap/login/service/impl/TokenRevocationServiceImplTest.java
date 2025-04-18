package com.java.fiap.login.service.impl;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.java.fiap.login.application.dto.InfoUserKeycloakDTO;
import com.java.fiap.login.domain.model.UserTokenRevoked;
import com.java.fiap.login.domain.repository.UserTokenRevokedRepository;
import com.java.fiap.login.service.TokenRevocationService;
import com.java.fiap.login.service.cache.UserTokenRevocationMemoryCache;
import com.java.fiap.login.utils.UserHelper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.keycloak.representations.AccessToken;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

class TokenRevocationServiceImplTest {

    AutoCloseable openMocks;
    private TokenRevocationService tokenRevocationService;
    @Mock
    private UserTokenRevokedRepository userTokenRevokedRepository;
    @Mock private UserTokenRevocationMemoryCache userTokenRevocationMemoryCache;

    @BeforeEach
    void setUp() {
        openMocks = MockitoAnnotations.openMocks(this);
        this.tokenRevocationService =
        new TokenRevocationServiceImpl(userTokenRevokedRepository, userTokenRevocationMemoryCache);
    }

    @AfterEach
    void tearDown() throws Exception {
        openMocks.close();
    }

    @Test
    void deveRevogarToken() {
        InfoUserKeycloakDTO infoUserKeycloakDTO = UserHelper.gerarInfoUserKeycloakDTO();
        AccessToken accessToken = mock(AccessToken.class);
        when(accessToken.getId()).thenReturn("tokenId");
        when(accessToken.getExp()).thenReturn(1234567890L);

        when(userTokenRevokedRepository.save(any(UserTokenRevoked.class))).thenAnswer(p -> p.getArgument(0));
        doNothing().when(userTokenRevocationMemoryCache).revokeToken(anyString());

        tokenRevocationService.revokeToken(infoUserKeycloakDTO, accessToken);

        verify(userTokenRevokedRepository, times(1)).save(any(UserTokenRevoked.class));
        verify(userTokenRevocationMemoryCache, times(1)).revokeToken(anyString());
    }
}
