package com.java.fiap.login.domain.repository;

import com.java.fiap.login.domain.model.UserLogin;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserLoginRepository extends JpaRepository<UserLogin, String> {}
