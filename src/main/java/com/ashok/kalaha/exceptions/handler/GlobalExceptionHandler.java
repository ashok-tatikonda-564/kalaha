package com.ashok.kalaha.exceptions.handler;

import com.ashok.kalaha.dto.ErrorDetails;
import com.ashok.kalaha.exceptions.*;
import java.time.LocalDateTime;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

  @ExceptionHandler(
      value = {
        GameException.class,
        GameCompletedException.class,
        SowingFromLargerPitException.class,
        NotYourTurnException.class,
        SowingFromEmptyPitException.class
      })
  @ResponseStatus(HttpStatus.BAD_REQUEST)
  protected ResponseEntity<?> handleGameExceptions(Exception ex, WebRequest request) {
    return logAndBuildResponseEntity(ex, request, HttpStatus.BAD_REQUEST);
  }

  @ExceptionHandler(GameNotFoundException.class)
  @ResponseStatus(HttpStatus.NOT_FOUND)
  public ResponseEntity<?> handleGameNotFoundException(
      GameNotFoundException ex, WebRequest request) {
    return logAndBuildResponseEntity(ex, request, HttpStatus.NOT_FOUND);
  }

  @ExceptionHandler(Exception.class)
  @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
  public ResponseEntity<?> handleUnhandledExceptions(Exception ex, WebRequest request) {
    return logAndBuildResponseEntity(ex, request, HttpStatus.INTERNAL_SERVER_ERROR);
  }

  private ResponseEntity<?> logAndBuildResponseEntity(
      Exception ex, WebRequest request, HttpStatus httpStatus) {
    var errorDetails =
        new ErrorDetails(
            ex.getMessage(),
            request.getDescription(false),
            ex.getClass().getSimpleName(),
            LocalDateTime.now());
    log.error(errorDetails.toString());
    return new ResponseEntity<>(errorDetails, httpStatus);
  }
}
