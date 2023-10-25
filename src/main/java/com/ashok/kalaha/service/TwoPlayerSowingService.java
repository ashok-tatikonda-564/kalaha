package com.ashok.kalaha.service;

import static com.ashok.kalaha.model.GameConstants.emptyStone;

import com.ashok.kalaha.api.SowingService;
import com.ashok.kalaha.exceptions.*;
import com.ashok.kalaha.model.*;
import java.util.function.Supplier;
import java.util.stream.Stream;
import org.springframework.stereotype.Service;

@Service
public class TwoPlayerSowingService implements SowingService {

  public static int totalPits = 14;
  public static int playerOneLargerPit = 7;
  public static int playerTwoLargerPit = 14;

  @Override
  public Game sow(Game game, int requestedPitId) {
    Pit selectedPit = game.getPit(requestedPitId);

    checkForExceptions(game, requestedPitId, selectedPit);
    startGameIfNotStarted(game, requestedPitId);
    startSowing(game, selectedPit);

    boolean isGameCompleted = checkIfGameCompleted(game);
    if (isGameCompleted) setWinnerAncCloseTheGame(game);

    return game;
  }

  private void checkForExceptions(Game game, int requestedPitId, Pit selectedPit) {

    if (game.getGameStatus() == GameStatus.COMPLETED
        || game.getGameStatus() == GameStatus.COMPLETED_DRAW)
      throw new GameCompletedException("Sowing not allowed on completed game.");

    if (requestedPitId == playerOneLargerPit || requestedPitId == playerTwoLargerPit)
      throw new SowingFromLargerPitException("sowing stones from larger pit is not allowed");

    if (game.getPlayerTurn() == PlayerTurn.PLAYER_ONE_TURN && requestedPitId > playerOneLargerPit
        || game.getPlayerTurn() == PlayerTurn.PLAYER_TWO_TURN
            && requestedPitId < playerOneLargerPit)
      throw new NotYourTurnException(
          "It's not your turn, please wait until the opponent finish their turn");

    if (selectedPit.getStones() == emptyStone)
      throw new SowingFromEmptyPitException("can't select empty pit for sowing");
  }

  private void startGameIfNotStarted(Game game, int requestedPitId) {
    if (game.getGameStatus() == GameStatus.CREATED) game.setGameStatus(GameStatus.IN_PROGRESS);

    if (game.getPlayerTurn() == null) {
      if (requestedPitId < playerOneLargerPit) game.setPlayerTurn(PlayerTurn.PLAYER_ONE_TURN);
      else game.setPlayerTurn(PlayerTurn.PLAYER_TWO_TURN);
    }
  }

  private void startSowing(Game game, Pit selectedPit) {
    int currentPitIndex = sowAllStonesExceptLastOne(game, selectedPit);

    int lastPitIndex = sowLastStone(game, currentPitIndex);

    if (lastPitIndex != playerOneLargerPit && lastPitIndex != playerTwoLargerPit)
      game.setPlayerTurn(nextTurn(game.getPlayerTurn()));
  }

  private int sowAllStonesExceptLastOne(Game game, Pit selectedPit) {
    int currentPitIndex = selectedPit.getPitId();
    for (int i = 1; i <= selectedPit.getStones() - 1; i++) {
      currentPitIndex = calculateNextPitIndex(game, currentPitIndex);
      game.getPit(currentPitIndex).sow();
    }
    selectedPit.clear();
    return calculateNextPitIndex(game, currentPitIndex);
  }

  private int sowLastStone(Game game, int currentPitIndex) {

    Pit targetPit = game.getPit(currentPitIndex);
    if (currentPitIndex == playerOneLargerPit || currentPitIndex == playerTwoLargerPit) {
      targetPit.sow();
    } else {
      Pit oppositePit = game.getPit(totalPits - currentPitIndex);
      boolean isEligibleForGrabbingOtherPlayerStones =
          checkIfTargetPitIsEmptyAndOppositePitIsNonEmptyOfCurrentPlayer(
              targetPit, oppositePit, game.getPlayerTurn());

      if (isEligibleForGrabbingOtherPlayerStones)
        grabOppositePitStonesAndAddItToCurrentPlayerLargerPit(game, currentPitIndex, oppositePit);
      else targetPit.sow();
    }

    return currentPitIndex;
  }

  private int calculateNextPitIndex(Game game, int requestedPitId) {
    PlayerTurn playerTurn = game.getPlayerTurn();
    int currentPitIndex = requestedPitId % totalPits + 1;

    if (isItLargerPitOfOppositePlayer(currentPitIndex, playerTurn))
      currentPitIndex = currentPitIndex % totalPits + 1;

    return currentPitIndex;
  }

  private boolean isItLargerPitOfOppositePlayer(int currentPitIndex, PlayerTurn playerTurn) {
    return ((currentPitIndex == playerOneLargerPit && playerTurn == PlayerTurn.PLAYER_TWO_TURN)
        || (currentPitIndex == playerTwoLargerPit && playerTurn == PlayerTurn.PLAYER_ONE_TURN));
  }

  private boolean checkIfTargetPitIsEmptyAndOppositePitIsNonEmptyOfCurrentPlayer(
      Pit targetPit, Pit oppositePit, PlayerTurn playerTurn) {
    return (targetPit.isEmpty() && !oppositePit.isEmpty())
        && ((targetPit.getPitId() < playerOneLargerPit && playerTurn == PlayerTurn.PLAYER_ONE_TURN)
            || (targetPit.getPitId() > playerOneLargerPit
                && playerTurn == PlayerTurn.PLAYER_TWO_TURN));
  }

  private static void grabOppositePitStonesAndAddItToCurrentPlayerLargerPit(
      Game game, int currentPitIndex, Pit oppositePit) {
    Integer oppositeStones = oppositePit.getStones();
    oppositePit.clear();
    int largerPitIndex =
        currentPitIndex < playerOneLargerPit ? playerOneLargerPit : playerTwoLargerPit;
    Pit largerPit = game.getPit(largerPitIndex);
    largerPit.addStones(oppositeStones + 1);
  }

  private void setWinnerAncCloseTheGame(Game game) {
    int playerOneScore = game.getPits().stream().limit(7).mapToInt(Pit::getStones).sum();
    int playerTwoScore = game.getPits().stream().skip(7).mapToInt(Pit::getStones).sum();

    if (playerOneScore == playerTwoScore) game.setGameStatus(GameStatus.COMPLETED_DRAW);
    else {
      Player winner = playerOneScore > playerTwoScore ? Player.PLAYER_ONE : Player.PLAYER_TWO;
      game.setWinner(winner);
      game.setGameStatus(GameStatus.COMPLETED);
    }
  }

  private boolean checkIfGameCompleted(Game game) {
    Supplier<Stream<Pit>> normalPitsStreamSupplier =
        () -> game.getPits().stream().filter(pit -> pit.getPitId() != 7 && pit.getPitId() != 14);

    var playerOnePitsStream = normalPitsStreamSupplier.get().limit(6);
    var playerTwoPitsStream = normalPitsStreamSupplier.get().skip(6);

    return (playerOnePitsStream.allMatch(pit -> pit.getStones() == 0)
        || playerTwoPitsStream.allMatch(pit -> pit.getStones() == 0));
  }

  public PlayerTurn nextTurn(PlayerTurn currentTurn) {
    if (currentTurn == PlayerTurn.PLAYER_ONE_TURN) return PlayerTurn.PLAYER_TWO_TURN;
    return PlayerTurn.PLAYER_ONE_TURN;
  }
}
