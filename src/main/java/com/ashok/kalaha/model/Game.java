package com.ashok.kalaha.model;

import com.ashok.kalaha.exceptions.GameException;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import java.util.stream.IntStream;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Setter
@Getter
@Document(collection = "games")
@RequiredArgsConstructor
public class Game {
  @Schema(name = "gameId", description = "id of the game", example = "gh45fdfdg3534ytenda")
  @Id
  private String gameId;

  @Schema(name = "pits", description = "Contains pits information like pidId and stones in it")
  private List<Pit> pits;

  @Schema(
      name = "playerTurn",
      description = "Indicates which player turn it is, to help who has to play next",
      example = "PLAYER_ONE_TURN")
  private PlayerTurn playerTurn;

  @Schema(
      name = "gameStatus",
      description =
          "Indicates status of game to help players understand if its started or in progress or completed",
      example = "IN_PROGRESS")
  private GameStatus gameStatus;

  @Schema(
      name = "winner",
      description = "Indicates winner when game is completed.",
      example = "PLAYER_TWO")
  private Player winner;

  @Schema(
      name = "numberOfPlayers",
      description = "Indicates how many players are playing the game",
      example = "2")
  private Integer numberOfPlayers;

  public Game(int stones) {
    this(stones, 2);
  }

  public Game(int stones, int numOfPlayers) {
    this.pits =
        IntStream.rangeClosed(1, 7 * numOfPlayers)
            .mapToObj(
                pitNumber ->
                    (pitNumber % 7 == 0) ? new LargerPit(pitNumber) : new Pit(pitNumber, stones))
            .toList();
    this.gameStatus = GameStatus.CREATED;
    this.numberOfPlayers = numOfPlayers;
  }

  public Pit getPit(int requestedPitId) {
    try {
      return this.pits.get(requestedPitId - 1);
    } catch (Exception e) {
      throw new GameException("Invalid pitIndex:" + requestedPitId + " has given!");
    }
  }
}
