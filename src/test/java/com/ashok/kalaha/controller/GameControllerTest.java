package com.ashok.kalaha.controller;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.ashok.kalaha.dto.ErrorDetails;
import com.ashok.kalaha.exceptions.GameNotFoundException;
import com.ashok.kalaha.model.Game;
import com.ashok.kalaha.model.GameStatus;
import com.ashok.kalaha.repository.GameRepository;
import com.ashok.kalaha.service.GameServiceImpl;
import com.ashok.kalaha.service.TwoPlayerSowingService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(controllers = GameController.class)
public class GameControllerTest {
  public static final int DEFAULT_STONES = 6;
  public static final int DEFAULT_NUM_OF_PLAYERS = 2;
  @Autowired private MockMvc mockMvc;

  @MockBean private GameServiceImpl gameServiceImpl;

  @MockBean private TwoPlayerSowingService twoPlayerSowingService;

  @MockBean private GameRepository gameRepository;

  @Autowired ObjectMapper objectMapper;

  private static final String GAME_URL = "/v1/api/games";
  private static final String LOAD_GAME_URL = GAME_URL + "/{gameId}";
  private static final String SOW_URL = GAME_URL + "/{gameId}/pits/{pitId}";

  private static final String DEFAULT_GAME_ID = "defaultGameId";
  public static final String INVALID_GAME_ID = "invalidGameId";

  @Test
  public void shouldCreateAndReturnTheGame() throws Exception {
    var mockedGame = buildAGame();
    when(gameServiceImpl.createGame(DEFAULT_STONES, DEFAULT_NUM_OF_PLAYERS)).thenReturn(mockedGame);

    mockMvc
        .perform(post(GAME_URL))
        .andExpect(status().is2xxSuccessful())
        .andExpect(
            response -> {
              var game =
                  objectMapper.readValue(response.getResponse().getContentAsString(), Game.class);

              assertNotNull(game);
              assertNotNull(game.getPits());
              assertNull(game.getWinner());
              assertEquals(GameStatus.CREATED, game.getGameStatus());
            });

    verify(gameServiceImpl, Mockito.times(1)).createGame(DEFAULT_STONES, DEFAULT_NUM_OF_PLAYERS);
  }

  @Test
  public void shouldCreateGameWithSuppliedPitStonesAndReturnTheGame() throws Exception {
    var customPitStones = 4;
    var mockedGame = new Game(customPitStones);
    mockedGame.setGameId(DEFAULT_GAME_ID);

    when(gameServiceImpl.createGame(customPitStones, DEFAULT_NUM_OF_PLAYERS))
        .thenReturn(mockedGame);

    mockMvc
        .perform(post(GAME_URL + "?stones=" + customPitStones))
        .andExpect(status().is2xxSuccessful())
        .andExpect(
            response -> {
              var game =
                  objectMapper.readValue(response.getResponse().getContentAsString(), Game.class);

              assertNotNull(game);
              assertNotNull(game.getPits());
              assertNull(game.getWinner());
              assertEquals(GameStatus.CREATED, game.getGameStatus());
              assertEquals(4, game.getPit(2).getStones());
            });

    verify(gameServiceImpl, Mockito.times(1)).createGame(customPitStones, DEFAULT_NUM_OF_PLAYERS);
  }

  @Test
  public void shouldNotAllowInvalidCustomPitStonesToCreateGame() throws Exception {
    var customPitStones = 0;

    mockMvc
        .perform(post(GAME_URL + "?stones=" + customPitStones))
        .andExpect(status().is4xxClientError())
        .andExpect(
            errorResponse -> {
              var errorDetails =
                  objectMapper.readValue(
                      errorResponse.getResponse().getContentAsString(), ErrorDetails.class);
              assertEquals("pit stones can't be zero or negative.", errorDetails.getMessage());
              assertEquals("GameException", errorDetails.getExceptionType());
            });
  }

  @Test
  public void shouldNotAllowInvalidNumberOfPlayersToCreateGame() throws Exception {
    var numberOfPlayers = -3;

    mockMvc
        .perform(post(GAME_URL + "?numberOfPlayers=" + numberOfPlayers))
        .andExpect(status().is4xxClientError())
        .andExpect(
            errorResponse -> {
              var errorDetails =
                  objectMapper.readValue(
                      errorResponse.getResponse().getContentAsString(), ErrorDetails.class);
              assertEquals(
                  "number of players can't be zero or negative and can't be greater than 10",
                  errorDetails.getMessage());
              assertEquals("GameException", errorDetails.getExceptionType());
            });
  }

  @Test
  public void shouldLoadExistingGame() throws Exception {
    var mockedGame = buildAGame();
    when(gameServiceImpl.loadGame(DEFAULT_GAME_ID)).thenReturn(mockedGame);

    mockMvc
        .perform(get(LOAD_GAME_URL, DEFAULT_GAME_ID))
        .andExpect(status().is2xxSuccessful())
        .andExpect(
            response -> {
              var game =
                  objectMapper.readValue(response.getResponse().getContentAsString(), Game.class);

              assertNotNull(game);
              assertNotNull(game.getPits());
              assertNull(game.getWinner());
              assertEquals(GameStatus.CREATED, game.getGameStatus());
            });

    verify(gameServiceImpl, Mockito.times(1)).loadGame(DEFAULT_GAME_ID);
  }

  @Test
  public void shouldReturnGameNotFoundForInvalidGameId() throws Exception {
    when(gameServiceImpl.loadGame(INVALID_GAME_ID))
        .thenThrow(new GameNotFoundException("Game not found with gameId: invalidGameId"));

    mockMvc
        .perform(get(LOAD_GAME_URL, INVALID_GAME_ID))
        .andExpect(status().isNotFound())
        .andExpect(
            errorResponse -> {
              var errorDetails =
                  objectMapper.readValue(
                      errorResponse.getResponse().getContentAsString(), ErrorDetails.class);
              assertEquals("Game not found with gameId: invalidGameId", errorDetails.getMessage());
              assertEquals("uri=/v1/api/games/invalidGameId", errorDetails.getDetails());
              assertEquals("GameNotFoundException", errorDetails.getExceptionType());
            });
    verify(gameServiceImpl, Mockito.times(1)).loadGame(INVALID_GAME_ID);
  }

  @Test
  public void shouldNotAllowSowingOnInvalidGame() throws Exception {
    when(gameServiceImpl.loadGame(INVALID_GAME_ID))
        .thenThrow(new GameNotFoundException("Game not found with gameId: invalidGameId"));

    mockMvc
        .perform(put(SOW_URL, INVALID_GAME_ID, 3))
        .andExpect(status().isNotFound())
        .andExpect(
            errorResponse -> {
              var errorDetails =
                  objectMapper.readValue(
                      errorResponse.getResponse().getContentAsString(), ErrorDetails.class);
              assertEquals("Game not found with gameId: invalidGameId", errorDetails.getMessage());
              assertEquals("uri=/v1/api/games/invalidGameId/pits/3", errorDetails.getDetails());
              assertEquals("GameNotFoundException", errorDetails.getExceptionType());
            });

    verify(gameServiceImpl, Mockito.times(1)).loadGame(INVALID_GAME_ID);
  }

  @Test
  public void shouldNotAllowSowingWithInvalidPitId() throws Exception {
    mockMvc
        .perform(put(SOW_URL, DEFAULT_GAME_ID, -5))
        .andExpect(status().is4xxClientError())
        .andExpect(
            errorResponse -> {
              var errorDetails =
                  objectMapper.readValue(
                      errorResponse.getResponse().getContentAsString(), ErrorDetails.class);
              assertEquals(
                  "Invalid pit selected. The pit should be selected between 1 to 6 or 8 to 13 or etc..",
                  errorDetails.getMessage());
              assertEquals("uri=/v1/api/games/defaultGameId/pits/-5", errorDetails.getDetails());
              assertEquals("GameException", errorDetails.getExceptionType());
            });
  }

  @Test
  public void shouldBeAbleToSowStonesForValidGame() throws Exception {
    var mockedGame = buildAGame();
    when(gameServiceImpl.loadGame(DEFAULT_GAME_ID)).thenReturn(mockedGame);
    when(twoPlayerSowingService.sow(mockedGame, 3)).thenReturn(mockedGame);
    when(gameServiceImpl.updateGame(mockedGame)).thenReturn(mockedGame);

    mockMvc
        .perform(put(SOW_URL, DEFAULT_GAME_ID, 3))
        .andExpect(status().is2xxSuccessful())
        .andExpect(
            response -> {
              var game =
                  objectMapper.readValue(response.getResponse().getContentAsString(), Game.class);

              assertNotNull(game);
              assertNotNull(game.getPits());
              assertNull(game.getWinner());
            });

    verify(gameServiceImpl, Mockito.times(1)).loadGame(DEFAULT_GAME_ID);
    verify(gameServiceImpl, Mockito.times(1)).updateGame(mockedGame);
  }

  private Game buildAGame() {
    var game = new Game(DEFAULT_STONES, DEFAULT_NUM_OF_PLAYERS);
    game.setGameId(DEFAULT_GAME_ID);

    return game;
  }
}
