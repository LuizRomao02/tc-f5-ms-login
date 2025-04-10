package com.java.fiap.login.domain.repository;

import com.java.fiap.login.domain.model.UserTokenRevoked;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserTokenRevokedRepository extends JpaRepository<UserTokenRevoked, String> {

  Optional<UserTokenRevoked> findByTokenId(String tokenId);

  boolean existsByTokenId(String tokenId);
}
