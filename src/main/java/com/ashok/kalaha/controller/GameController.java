package com.ashok.kalaha.controller;

import com.ashok.kalaha.api.GameService;
import com.ashok.kalaha.api.SowingService;
import com.ashok.kalaha.dto.ErrorDetails;
import com.ashok.kalaha.exceptions.GameException;
import com.ashok.kalaha.model.Game;
import com.ashok.kalaha.model.GameConstants;
import com.ashok.kalaha.service.TwoPlayerSowingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/v1/api/games")
@CrossOrigin
@AllArgsConstructor
public class GameController {
  private GameService gameService;
  private SowingService sowingService;

  @Operation(summary = "Creates new kalaha game with 6 pit stones by default.")
  @Parameter(
      in = ParameterIn.QUERY,
      description = "Can be used to alter default game pit stones to another value",
      name = "stones",
      content = @Content(schema = @Schema(type = "integer")))
  @Parameter(
      in = ParameterIn.QUERY,
      description =
          "can be used to alter number of players that can be played at once. currently its support only two players."
              + "For any other valid value, it will be modified to two players until its extended.",
      name = "numOfPlayers",
      content = @Content(schema = @Schema(type = "integer")))
  @ApiResponses(
      value = {
        @ApiResponse(
            responseCode = "200",
            description = "Responds with game data that's newly created",
            content = {
              @Content(
                  mediaType = "application/json",
                  schema = @Schema(implementation = Game.class))
            }),
        @ApiResponse(
            responseCode = "400",
            description = "client error, responds with all required error information",
            content =
                @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = ErrorDetails.class))),
        @ApiResponse(
            responseCode = "500",
            description = "server error, responds with all required error information",
            content =
                @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = ErrorDetails.class)))
      })
  @PostMapping
  public ResponseEntity<Game> createGame(
      @RequestParam(value = "stones", required = false) Integer stones,
      @RequestParam(value = "numOfPlayers", required = false) Integer numOfPlayers) {
    log.info("Invoking createGame() endpoint... ");
    int pitStones = stones != null ? stones : GameConstants.defaultPitStones;
    int players = numOfPlayers != null ? numOfPlayers : GameConstants.defaultNumberOfPlayers;

    if (pitStones <= 0) throw new GameException("pit stones can't be zero or negative.");
    if (players <= 0 || players > 10)
      throw new GameException(
          "number of players can't be zero or negative or can't be greater than 10");

    int currentlySupportedNumberOfPlayers = GameConstants.defaultNumberOfPlayers;

    return ResponseEntity.ok(gameService.createGame(pitStones, currentlySupportedNumberOfPlayers));
  }

  @Operation(summary = "Get a game by its id")
  @ApiResponses(
      value = {
        @ApiResponse(
            responseCode = "200",
            description = "Responds with game data found with the id",
            content = {
              @Content(
                  mediaType = "application/json",
                  schema = @Schema(implementation = Game.class))
            }),
        @ApiResponse(
            responseCode = "404",
            description = "Game not found with given id",
            content =
                @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = ErrorDetails.class))),
        @ApiResponse(
            responseCode = "500",
            description = "server error, responds with all required error information",
            content =
                @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = ErrorDetails.class)))
      })
  @GetMapping(value = "/{gameId}")
  public ResponseEntity<Game> loadGame(@PathVariable(value = "gameId") String gameId) {
    log.info("loading game with gameId: " + gameId);
    return ResponseEntity.ok(gameService.loadGame(gameId));
  }

  @Operation(summary = "Sow stones from pit of the game")
  @ApiResponses(
      value = {
        @ApiResponse(
            responseCode = "200",
            description =
                "Responds with updated game data after successful sow of stones from the pit",
            content = {
              @Content(
                  mediaType = "application/json",
                  schema = @Schema(implementation = Game.class))
            }),
        @ApiResponse(
            responseCode = "404",
            description = "Game not found with given id",
            content =
                @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = ErrorDetails.class))),
        @ApiResponse(
            responseCode = "400",
            description = "client error, responds with all required error information",
            content =
                @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = ErrorDetails.class))),
        @ApiResponse(
            responseCode = "500",
            description = "server error, responds with all required error information",
            content =
                @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = ErrorDetails.class)))
      })
  @Parameter(
      in = ParameterIn.PATH,
      description = "id of the game for which you want to sow the stones",
      name = "gameId",
      content = @Content(schema = @Schema(type = "string")))
  @Parameter(
      in = ParameterIn.PATH,
      description = "pitId of the game from which you want to sow the stones",
      name = "pitId",
      content = @Content(schema = @Schema(type = "string")))
  @PutMapping(value = "/{gameId}/pits/{pitId}")
  public ResponseEntity<Game> sowStone(
      @PathVariable(value = "gameId") String gameId, @PathVariable(value = "pitId") Integer pitId) {
    log.info("sowing for GameId: " + gameId + "  , pit Index: " + pitId);

    if (pitId == null || pitId < 1 || pitId % 7 == 0)
      throw new GameException(
          "Invalid pit selected. The pit should be selected between 1 to 6 or 8 to 13 or etc..");

    Game game = gameService.loadGame(gameId);

    if (pitId >= game.getPits().size())
      throw new GameException(
          "Invalid pit selected. The pitId should be one of existing valid pit");

    game = getSowingService(game).sow(game, pitId);
    return ResponseEntity.ok(gameService.updateGame(game));
  }

  public SowingService getSowingService(Game game) {
    if (game.getNumberOfPlayers() == 2) return new TwoPlayerSowingService();
    else throw new GameException("currently only two players are supported");
  }
}
