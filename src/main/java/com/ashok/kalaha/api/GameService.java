package com.ashok.kalaha.api;

import com.ashok.kalaha.model.Game;

public interface GameService {
  Game createGame(int stones, int numOfPlayers);

  Game loadGame(String gameId);

  Game updateGame(Game game);
}
