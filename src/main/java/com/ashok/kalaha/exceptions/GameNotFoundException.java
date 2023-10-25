package com.ashok.kalaha.exceptions;

public class GameNotFoundException extends RuntimeException {
  public GameNotFoundException(String message) {
    super(message);
  }
}
