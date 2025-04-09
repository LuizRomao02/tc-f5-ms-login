package com.java.fiap.login.service.usecase;

public interface UseCase<I, O> {
  O execute(I input);
}
