package com.ashok.kalaha.integration;

import static org.junit.jupiter.api.Assertions.*;

import com.ashok.kalaha.config.MongoDBTestContainer;
import com.ashok.kalaha.exceptions.GameNotFoundException;
import com.ashok.kalaha.model.Game;
import com.ashok.kalaha.model.PlayerTurn;
import com.ashok.kalaha.service.GameServiceImpl;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class GameServiceImplIntegrationTest implements MongoDBTestContainer {
  @Autowired GameServiceImpl gameServiceImpl;

  private final String defaultGameId = "defaultGameId";

  private Game getAGame() {
    return new Game(6);
  }

  private Game updateTheGame(Game game) {
    game.setGameId(defaultGameId);
    game.setPlayerTurn(PlayerTurn.PLAYER_TWO_TURN);
    game.getPit(3).setStones(9);

    return game;
  }

  @Test
  public void serviceShouldBeAbleToCreateNewGame() {
    var newGame = gameServiceImpl.createGame(6, 2);
    assertNotNull(newGame);
  }

  @Test
  public void serviceShouldBeAbleToSaveUpdatedGame() {
    var game = getAGame();
    var updatedGame = updateTheGame(game);

    Game updated = gameServiceImpl.updateGame(updatedGame);

    assertNotNull(updatedGame);
    assertEquals(defaultGameId, updated.getGameId());
    assertEquals(PlayerTurn.PLAYER_TWO_TURN, updated.getPlayerTurn());
    assertEquals(9, updated.getPit(3).getStones());
    assertEquals(6, updated.getPit(1).getStones());
  }

  @Test
  public void serviceShouldBeAbleLoadExistingGame() {
    var game = getAGame();
    var updatedGame = updateTheGame(game);
    gameServiceImpl.updateGame(updatedGame);

    var gameFromDB = gameServiceImpl.loadGame(defaultGameId);
    assertNotNull(gameFromDB);
  }

  @Test
  public void serviceShouldThrowExceptionForLoadingGameWithInvalidGameId() {
    var gameId = "nonExistingGameId";
    var exception =
        assertThrows(GameNotFoundException.class, () -> gameServiceImpl.loadGame(gameId));
    assertEquals("Game not found with gameId: " + gameId, exception.getMessage());
  }
}
