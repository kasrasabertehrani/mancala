package com.mancalagame.domain.model;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Pit {

    private int stones;

    public Pit(int startingStones) {
        this.stones = startingStones;
    }

    public void addStone() {
        this.stones++;
    }

    public void addStones(int amount) {
        this.stones += amount;
    }

    public int pickAllStones() {
        int pickedUp = this.stones;
        this.stones = 0;
        return pickedUp;
    }
}