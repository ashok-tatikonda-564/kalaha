package com.ashok.kalaha.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ErrorDetails {
  @Schema(
      name = "message",
      description = "error message, indicates what went wrong with the request",
      example = "It's not your turn, please wait until the opponent finish their turn")
  private String message;

  @Schema(
      name = "details",
      description = "Some extra details like url for which error occurred",
      example = "uri=/v1/api/games/6532b7a7715cf22387936b88/pits/1")
  private String details;

  @Schema(
      name = "exceptionType",
      description =
          "Indicates exception name, that enables client to have functionality based on exception type",
      example = "com.ashok.kalaha.exceptions.SowingFromEmptyPitException")
  private String exceptionType;

  @Schema(
      name = "timestamp",
      description = "Indicates when the exception occurred",
      example = "2023-10-20T17:24:04.763643091")
  private LocalDateTime timestamp;
}
