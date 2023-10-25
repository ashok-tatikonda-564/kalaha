package com.ashok.kalaha.integration;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.ashok.kalaha.config.MongoDBTestContainer;
import com.ashok.kalaha.dto.ErrorDetails;
import com.ashok.kalaha.model.Game;
import com.ashok.kalaha.model.GameStatus;
import com.ashok.kalaha.model.PlayerTurn;
import com.ashok.kalaha.repository.GameRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
public class KalahaGameIntegrationTest implements MongoDBTestContainer {
  private static final String GAME_URL = "/v1/api/games";
  private static final String LOAD_GAME_URL = GAME_URL + "/{gameId}";
  private static final String SOW_URL = GAME_URL + "/{gameId}/pits/{pitId}";

  @Autowired private MockMvc mockMvc;

  @Autowired private ObjectMapper objectMapper;

  @Autowired private GameRepository gameRepository;

  @BeforeEach
  public void clearDataInDb() {
    gameRepository.deleteAll();
  }

  @Test
  public void shouldCreateAndReturnTheGameForCreateNewGameRequest() throws Exception {
    assertEquals(gameRepository.findAll().size(), 0);
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
    assertEquals(gameRepository.findAll().size(), 1);
  }

  @Test
  public void shouldReturnGameForValidGameId() throws Exception {
    var savedGame = createAGameInDBWithDefaultPitStones();
    assertEquals(gameRepository.findAll().size(), 1);

    mockMvc
        .perform(get(LOAD_GAME_URL, savedGame.getGameId()))
        .andExpect(status().is2xxSuccessful())
        .andExpect(
            response -> {
              var game =
                  objectMapper.readValue(response.getResponse().getContentAsString(), Game.class);
              assertNotNull(game);
              assertNotNull(game.getPits());
            });
  }

  @Test
  public void shouldReturnGameNotFoundForInvalidGameId() throws Exception {
    mockMvc
        .perform(get(LOAD_GAME_URL, "invalidGameId"))
        .andExpect(status().isNotFound())
        .andExpect(
            errorResponse -> {
              var errorDetails =
                  objectMapper.readValue(
                      errorResponse.getResponse().getContentAsString(), ErrorDetails.class);
              assertEquals("Game not found with gameId: invalidGameId", errorDetails.getMessage());
              assertEquals("uri=/v1/api/games/invalidGameId", errorDetails.getDetails());
            });
  }

  @Test
  public void shouldBeAbleToSow() throws Exception {
    var savedGame = createAGameInDBWithDefaultPitStones();
    mockMvc
        .perform(put(SOW_URL, savedGame.getGameId(), 3))
        .andExpect(status().is2xxSuccessful())
        .andExpect(
            response -> {
              var game =
                  objectMapper.readValue(response.getResponse().getContentAsString(), Game.class);

              assertNotNull(game);
              assertNotNull(game.getPits());
              assertNull(game.getWinner());
              assertEquals(GameStatus.IN_PROGRESS, game.getGameStatus());
              assertEquals(PlayerTurn.PLAYER_TWO_TURN, game.getPlayerTurn());
            });
  }

  private Game createAGameInDBWithDefaultPitStones() {
    return gameRepository.save(new Game(6));
  }
}
