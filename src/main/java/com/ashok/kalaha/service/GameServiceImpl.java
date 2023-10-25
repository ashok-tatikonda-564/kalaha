package com.ashok.kalaha.service;

import com.ashok.kalaha.api.GameService;
import com.ashok.kalaha.exceptions.GameNotFoundException;
import com.ashok.kalaha.model.Game;
import com.ashok.kalaha.repository.GameRepository;
import lombok.AllArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class GameServiceImpl implements GameService {
  private GameRepository gameRepository;

  @Override
  public Game createGame(int stones, int numOfPlayers) {
    Game newGame = new Game(stones, numOfPlayers);
    return gameRepository.save(newGame);
  }

  @CachePut(value = "games", key = "#game.gameId")
  @CacheEvict(
      value = "games",
      beforeInvocation = true,
      condition =
          "#game.getGameStatus() == GameStatus.COMPLETED || #game.getGameStatus() == GameStatus.COMPLETED_DRAW")
  @Override
  public Game updateGame(Game game) {
    return gameRepository.save(game);
  }

  @Cacheable(value = "games", key = "#gameId", unless = "#result  == null")
  @Override
  public Game loadGame(String gameId) {
    return gameRepository
        .findById(gameId)
        .orElseThrow(() -> new GameNotFoundException("Game not found with gameId: " + gameId));
  }
}
