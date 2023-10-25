package com.ashok.kalaha.repository;

import static org.junit.jupiter.api.Assertions.assertTrue;

import com.ashok.kalaha.config.MongoDBTestContainer;
import com.ashok.kalaha.model.Game;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class GameRepositoryTest implements MongoDBTestContainer {
  @Autowired GameRepository repository;

  @Test
  public void repositoryShouldBeAbleToSaveGame() {
    var game = new Game(6);
    repository.save(game);

    var gameFromDB = repository.findById(game.getGameId());
    assertTrue(gameFromDB.isPresent());
  }
}
