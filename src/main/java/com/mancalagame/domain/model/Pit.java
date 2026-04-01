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

    public void increment() {
        this.stones++;
    }

    // Adds multiple stones (used during a capture or the final sweep)
    public void addStones(int amount) {
        this.stones += amount;
    }

    // Empties the pit and tells you how many stones you just picked up
    public int clear() {
        int pickedUp = this.stones;
        this.stones = 0;
        return pickedUp;
    }
}