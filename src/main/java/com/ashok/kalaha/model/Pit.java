package com.ashok.kalaha.model;

import lombok.AllArgsConstructor;
import lombok.Data;

@AllArgsConstructor
@Data
public class Pit {
  Integer pitId;
  Integer stones;

  public Boolean isEmpty() {
    return this.stones == 0;
  }

  public void clear() {
    this.stones = 0;
  }

  public void sow() {
    this.stones++;
  }

  public void addStones(Integer stones) {
    this.stones += stones;
  }

  @Override
  public String toString() {
    return pitId + ":" + stones;
  }
}
