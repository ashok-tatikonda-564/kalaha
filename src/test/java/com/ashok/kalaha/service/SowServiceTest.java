package com.ashok.kalaha.service;

import static com.ashok.kalaha.constants.TwoPlayerGameConstants.*;
import static org.junit.jupiter.api.Assertions.*;

import com.ashok.kalaha.exceptions.*;
import com.ashok.kalaha.model.*;
import com.ashok.kalaha.repository.GameRepository;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class SowServiceTest {
  @Mock GameRepository gameRepository;

  @InjectMocks TwoPlayerSowingService twoPlayerSowingService;

  private final int defaultPitStone = 6;

  @Test
  public void shouldRespondWithSowingFromLargerPitException() {
    var game = new Game(defaultPitStone);
    assertThrows(
        SowingFromLargerPitException.class,
        () -> twoPlayerSowingService.sow(game, playerOneLargerPit));
    assertThrows(
        SowingFromLargerPitException.class,
        () -> twoPlayerSowingService.sow(game, playerTwoLargerPit));
  }

  @Test
  public void shouldRespondWithNotYourTurnException() {
    var game = new Game(defaultPitStone);
    game.setPlayerTurn(PlayerTurn.PLAYER_ONE_TURN);
    assertThrows(
        NotYourTurnException.class, () -> twoPlayerSowingService.sow(game, secondPitPlayerTwo));

    game.setPlayerTurn(PlayerTurn.PLAYER_TWO_TURN);
    assertThrows(
        NotYourTurnException.class, () -> twoPlayerSowingService.sow(game, firstPitPlayerOne));
  }

  @Test
  public void shouldRespondWithSowingFromEmptyPitException() {
    var game = getAGame();
    game.getPit(secondPitPlayerOne).setStones(0);
    assertThrows(
        SowingFromEmptyPitException.class,
        () -> twoPlayerSowingService.sow(game, secondPitPlayerOne));
  }

  @Test
  public void shouldRespondWithGameCompletedException() {
    var game = getAGame();
    game.setGameStatus(GameStatus.COMPLETED);
    assertThrows(
        GameCompletedException.class, () -> twoPlayerSowingService.sow(game, firstPitPlayerOne));
  }

  @Test
  public void shouldStartGameIfNotStarted() {
    var game = getAGame();
    assertNull(game.getPlayerTurn());

    twoPlayerSowingService.sow(game, secondPitPlayerOne);

    assertEquals(GameStatus.IN_PROGRESS, game.getGameStatus());
    assertEquals(PlayerTurn.PLAYER_TWO_TURN, game.getPlayerTurn());
  }

  @Test
  public void shouldKeepPlayerTurnIfLastStoneFallsOnTheirLargerPit() {
    var game = getAGame();
    twoPlayerSowingService.sow(game, firstPitPlayerOne);

    assertEquals(GameStatus.IN_PROGRESS, game.getGameStatus());
    assertEquals(PlayerTurn.PLAYER_ONE_TURN, game.getPlayerTurn());

    var newGame = getAGame();
    twoPlayerSowingService.sow(newGame, firstPitPlayerTwo);

    assertEquals(GameStatus.IN_PROGRESS, newGame.getGameStatus());
    assertEquals(PlayerTurn.PLAYER_TWO_TURN, newGame.getPlayerTurn());
  }

  @Test
  public void shouldGrabOppositePitStones() {
    var game = setupAGameWithGrabOppositePitStonesConditions();

    assertEquals(0, game.getPit(playerOneLargerPit).getStones());
    assertEquals(6, game.getPit(fifthPitPlayerTwo).getStones());

    twoPlayerSowingService.sow(game, firstPitPlayerOne);

    verifyStatusOfPitsAfterAGrab(game);
  }

  @Test
  public void shouldBeAbleToAnnounceWinner() {
    var game = setupAGameWithCompletedStateConditions();
    assertNull(game.getWinner());

    twoPlayerSowingService.sow(game, sixthPitPlayerOne);

    assertEquals(Player.PLAYER_TWO, game.getWinner());
    assertEquals(GameStatus.COMPLETED, game.getGameStatus());
    assertEquals(PlayerTurn.PLAYER_TWO_TURN, game.getPlayerTurn());
  }

  @Test
  public void testSowOfStonesStartingWithPlayerOne() {
    var game = getAGame();
    Assertions.assertThat(game.getPits().toString())
        .isEqualTo("[1:6, 2:6, 3:6, 4:6, 5:6, 6:6, 7:0, 8:6, 9:6, 10:6, 11:6, 12:6, 13:6, 14:0]");
    game = twoPlayerSowingService.sow(game, 2);
    Assertions.assertThat(game.getPits().toString())
        .isEqualTo("[1:6, 2:0, 3:7, 4:7, 5:7, 6:7, 7:1, 8:7, 9:6, 10:6, 11:6, 12:6, 13:6, 14:0]");
    assertEquals(PlayerTurn.PLAYER_TWO_TURN, game.getPlayerTurn());

    game = twoPlayerSowingService.sow(game, 9);
    Assertions.assertThat(game.getPits().toString())
        .isEqualTo("[1:7, 2:0, 3:7, 4:7, 5:7, 6:7, 7:1, 8:7, 9:0, 10:7, 11:7, 12:7, 13:7, 14:1]");
    assertEquals(PlayerTurn.PLAYER_ONE_TURN, game.getPlayerTurn());

    game = twoPlayerSowingService.sow(game, 6);
    Assertions.assertThat(game.getPits().toString())
        .isEqualTo("[1:7, 2:0, 3:7, 4:7, 5:7, 6:0, 7:2, 8:8, 9:1, 10:8, 11:8, 12:8, 13:8, 14:1]");
    assertEquals(PlayerTurn.PLAYER_TWO_TURN, game.getPlayerTurn());

    game = twoPlayerSowingService.sow(game, 13);
    Assertions.assertThat(game.getPits().toString())
        .isEqualTo("[1:8, 2:1, 3:8, 4:8, 5:8, 6:1, 7:2, 8:9, 9:1, 10:8, 11:8, 12:8, 13:0, 14:2]");
    assertEquals(PlayerTurn.PLAYER_ONE_TURN, game.getPlayerTurn());

    game = twoPlayerSowingService.sow(game, 3);
    Assertions.assertThat(game.getPits().toString())
        .isEqualTo("[1:8, 2:1, 3:0, 4:9, 5:9, 6:2, 7:3, 8:10, 9:2, 10:9, 11:9, 12:8, 13:0, 14:2]");
    assertEquals(PlayerTurn.PLAYER_TWO_TURN, game.getPlayerTurn());

    game = twoPlayerSowingService.sow(game, 9);
    Assertions.assertThat(game.getPits().toString())
        .isEqualTo(
            "[1:8, 2:1, 3:0, 4:9, 5:9, 6:2, 7:3, 8:10, 9:0, 10:10, 11:10, 12:8, 13:0, 14:2]");
    assertEquals(PlayerTurn.PLAYER_ONE_TURN, game.getPlayerTurn());

    game = twoPlayerSowingService.sow(game, 2);
    Assertions.assertThat(game.getPits().toString())
        .isEqualTo(
            "[1:8, 2:0, 3:0, 4:9, 5:9, 6:2, 7:14, 8:10, 9:0, 10:10, 11:0, 12:8, 13:0, 14:2]");
    assertEquals(PlayerTurn.PLAYER_TWO_TURN, game.getPlayerTurn());
  }

  @Test
  public void testSowOfStonesStartingWithPlayerTwo() {
    var game = getAGame();
    Assertions.assertThat(game.getPits().toString())
        .isEqualTo("[1:6, 2:6, 3:6, 4:6, 5:6, 6:6, 7:0, 8:6, 9:6, 10:6, 11:6, 12:6, 13:6, 14:0]");
    game = twoPlayerSowingService.sow(game, 2);
    Assertions.assertThat(game.getPits().toString())
        .isEqualTo("[1:6, 2:0, 3:7, 4:7, 5:7, 6:7, 7:1, 8:7, 9:6, 10:6, 11:6, 12:6, 13:6, 14:0]");
    assertEquals(PlayerTurn.PLAYER_TWO_TURN, game.getPlayerTurn());

    game = twoPlayerSowingService.sow(game, 9);
    Assertions.assertThat(game.getPits().toString())
        .isEqualTo("[1:7, 2:0, 3:7, 4:7, 5:7, 6:7, 7:1, 8:7, 9:0, 10:7, 11:7, 12:7, 13:7, 14:1]");
    assertEquals(PlayerTurn.PLAYER_ONE_TURN, game.getPlayerTurn());

    game = twoPlayerSowingService.sow(game, 6);
    Assertions.assertThat(game.getPits().toString())
        .isEqualTo("[1:7, 2:0, 3:7, 4:7, 5:7, 6:0, 7:2, 8:8, 9:1, 10:8, 11:8, 12:8, 13:8, 14:1]");
    assertEquals(PlayerTurn.PLAYER_TWO_TURN, game.getPlayerTurn());

    game = twoPlayerSowingService.sow(game, 13);
    Assertions.assertThat(game.getPits().toString())
        .isEqualTo("[1:8, 2:1, 3:8, 4:8, 5:8, 6:1, 7:2, 8:9, 9:1, 10:8, 11:8, 12:8, 13:0, 14:2]");
    assertEquals(PlayerTurn.PLAYER_ONE_TURN, game.getPlayerTurn());

    game = twoPlayerSowingService.sow(game, 3);
    Assertions.assertThat(game.getPits().toString())
        .isEqualTo("[1:8, 2:1, 3:0, 4:9, 5:9, 6:2, 7:3, 8:10, 9:2, 10:9, 11:9, 12:8, 13:0, 14:2]");
    assertEquals(PlayerTurn.PLAYER_TWO_TURN, game.getPlayerTurn());

    game = twoPlayerSowingService.sow(game, 9);
    Assertions.assertThat(game.getPits().toString())
        .isEqualTo(
            "[1:8, 2:1, 3:0, 4:9, 5:9, 6:2, 7:3, 8:10, 9:0, 10:10, 11:10, 12:8, 13:0, 14:2]");
    assertEquals(PlayerTurn.PLAYER_ONE_TURN, game.getPlayerTurn());

    game = twoPlayerSowingService.sow(game, 2);
    Assertions.assertThat(game.getPits().toString())
        .isEqualTo(
            "[1:8, 2:0, 3:0, 4:9, 5:9, 6:2, 7:14, 8:10, 9:0, 10:10, 11:0, 12:8, 13:0, 14:2]");
    assertEquals(PlayerTurn.PLAYER_TWO_TURN, game.getPlayerTurn());

    game = twoPlayerSowingService.sow(game, 8);
    Assertions.assertThat(game.getPits().toString())
        .isEqualTo(
            "[1:9, 2:1, 3:1, 4:10, 5:9, 6:2, 7:14, 8:0, 9:1, 10:11, 11:1, 12:9, 13:1, 14:3]");
    assertEquals(PlayerTurn.PLAYER_ONE_TURN, game.getPlayerTurn());

    game = twoPlayerSowingService.sow(game, 1);
    Assertions.assertThat(game.getPits().toString())
        .isEqualTo(
            "[1:0, 2:2, 3:2, 4:11, 5:10, 6:3, 7:15, 8:1, 9:2, 10:12, 11:1, 12:9, 13:1, 14:3]");
    assertEquals(PlayerTurn.PLAYER_TWO_TURN, game.getPlayerTurn());

    game = twoPlayerSowingService.sow(game, 9);
    Assertions.assertThat(game.getPits().toString())
        .isEqualTo(
            "[1:0, 2:2, 3:2, 4:11, 5:10, 6:3, 7:15, 8:1, 9:0, 10:13, 11:2, 12:9, 13:1, 14:3]");
    assertEquals(PlayerTurn.PLAYER_ONE_TURN, game.getPlayerTurn());

    game = twoPlayerSowingService.sow(game, 2);
    Assertions.assertThat(game.getPits().toString())
        .isEqualTo(
            "[1:0, 2:0, 3:3, 4:12, 5:10, 6:3, 7:15, 8:1, 9:0, 10:13, 11:2, 12:9, 13:1, 14:3]");
    assertEquals(PlayerTurn.PLAYER_TWO_TURN, game.getPlayerTurn());

    game = twoPlayerSowingService.sow(game, 8);
    Assertions.assertThat(game.getPits().toString())
        .isEqualTo(
            "[1:0, 2:0, 3:3, 4:12, 5:0, 6:3, 7:15, 8:0, 9:0, 10:13, 11:2, 12:9, 13:1, 14:14]");
    assertEquals(PlayerTurn.PLAYER_ONE_TURN, game.getPlayerTurn());
  }

  private void verifyStatusOfPitsAfterAGrab(Game game) {
    assertEquals((6 + 1), game.getPit(playerOneLargerPit).getStones());
    assertEquals(0, game.getPit(fifthPitPlayerTwo).getStones());
    assertEquals(0, game.getPit(firstPitPlayerOne).getStones());
    assertEquals(0, game.getPit(secondPitPlayerOne).getStones());
    assertEquals(PlayerTurn.PLAYER_TWO_TURN, game.getPlayerTurn());
  }

  private Game getAGame() {
    var game = new Game(defaultPitStone);
    assertEquals(GameStatus.CREATED, game.getGameStatus());
    return game;
  }

  private Game setupAGameWithGrabOppositePitStonesConditions() {
    var game = getAGame();
    game.getPit(1).setStones(1);
    game.getPit(2).setStones(0);
    return game;
  }

  private Game setupAGameWithCompletedStateConditions() {
    var game = getAGame();
    game.getPit(1).setStones(0);
    game.getPit(2).setStones(0);
    game.getPit(3).setStones(0);
    game.getPit(4).setStones(0);
    game.getPit(5).setStones(0);
    game.getPit(6).setStones(4);

    game.getPit(7).setStones(25);
    game.getPit(14).setStones(10);

    return game;
  }
}
