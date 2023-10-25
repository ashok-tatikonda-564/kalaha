package com.ashok.kalaha.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.ashok.kalaha.exceptions.GameNotFoundException;
import com.ashok.kalaha.model.Game;
import com.ashok.kalaha.model.PlayerTurn;
import com.ashok.kalaha.repository.GameRepository;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class GameServiceImplTest {
  @Mock GameRepository gameRepository;

  @InjectMocks GameServiceImpl gameServiceImpl;

  private final String defaultGameId = "defaultGameId";

  @Test
  public void serviceShouldBeAbleToCreateNewGame() {
    when(gameRepository.save(any())).thenReturn(getAGame());

    var newGame = gameServiceImpl.createGame(6, 2);
    assertNotNull(newGame);
  }

  @Test
  public void serviceShouldBeAbleToSaveUpdatedGame() {
    Game game = getAGame();
    when(gameRepository.save(game)).thenReturn(game);

    var updatedGame = gameServiceImpl.updateGame(game);

    assertNotNull(updatedGame);
    assertEquals(defaultGameId, updatedGame.getGameId());
    assertEquals(PlayerTurn.PLAYER_TWO_TURN, updatedGame.getPlayerTurn());
    assertEquals(9, updatedGame.getPit(3).getStones());
    assertEquals(6, updatedGame.getPit(1).getStones());
  }

  @Test
  public void serviceShouldBeAbleLoadExistingGame() {
    Game game = new Game(6);
    when(gameRepository.findById(defaultGameId)).thenReturn(Optional.of(game));

    var gameFromDB = gameServiceImpl.loadGame(defaultGameId);
    assertNotNull(gameFromDB);
  }

  @Test
  public void serviceShouldThrowExceptionForLoadingGameWithInvalidGameId() {
    var gameId = "nonExistingGameId";
    when(gameRepository.findById(gameId)).thenReturn(Optional.empty());

    var exception =
        assertThrows(GameNotFoundException.class, () -> gameServiceImpl.loadGame(gameId));
    assertEquals("Game not found with gameId: " + gameId, exception.getMessage());
  }

  private Game getAGame() {
    Game game = new Game(6);

    game.setGameId(defaultGameId);
    game.setPlayerTurn(PlayerTurn.PLAYER_TWO_TURN);
    game.getPit(3).setStones(9);

    return game;
  }
}
