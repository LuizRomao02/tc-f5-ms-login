package com.java.fiap.login.utils;

import com.java.fiap.login.application.dto.InfoUserKeycloakDTO;
import com.java.fiap.login.application.dto.UserKeycloak;
import com.java.fiap.login.application.dto.UserLoginDTO;
import com.java.fiap.login.application.dto.UserRegisteredEvent;
import com.java.fiap.login.application.dto.enums.UserTypeEnum;
import com.java.fiap.login.domain.model.UserLogin;

public final class UserHelper {

    public static final String EMAIL = "fulano@gmail.com";
    public static final String USERNAME = "fulano123";
    public static final String PASSWORD = "password";
    public static final String FIRST_NAME = "Fulano";
    public static final String LAST_NAME = "Silva";

    private UserHelper() {
        throw new UnsupportedOperationException("This class cannot be instantiated.");
    }

    public static UserLogin gerarUserLogin() {
        return UserLogin.builder()
                .id("712f3cc-470a-4587-a64a-6a539e3b2304")
                .email(EMAIL)
                .username(USERNAME)
                .firstName(FIRST_NAME)
                .lastName(LAST_NAME)
                .userId("409834d9-ea5b-44c5-a4a1-2252239bb3e0")
                .type(UserTypeEnum.PATIENT)
                .build();
    }

    public static UserLoginDTO gerarUserLoginDTO() {
        return UserLoginDTO.builder()
                .username(USERNAME)
                .password(PASSWORD)
                .build();
    }

    public static UserRegisteredEvent gerarUserRegisteredEvent() {
        return UserRegisteredEvent.builder()
                .id("409834d9-ea5b-44c5-a4a1-2252239bb3e0")
                .name(FIRST_NAME + " " + LAST_NAME)
                .email(EMAIL)
                .password(PASSWORD)
                .firstName(FIRST_NAME)
                .lastName(LAST_NAME)
                .type(UserTypeEnum.PATIENT)
                .build();
    }

    public static UserKeycloak gerarUserKeycloak() {
        return UserKeycloak.builder()
                .id("d712f3cc-470a-4587-a64a-6a539e3b2304")
                .username(USERNAME)
                .email(EMAIL)
                .build();
    }

    public static InfoUserKeycloakDTO gerarInfoUserKeycloakDTO() {
        return InfoUserKeycloakDTO.builder()
                .userId("d712f3cc-470a-4587-a64a-6a539e3b2304")
                .build();
    }
}
