package com.mancalagame.domain.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class PitTest {

    @Test
    void shouldIncrementStonesByOne() {
        Pit pit = new Pit(3);

        pit.increment();

        assertEquals(4, pit.getStones());
    }

    @Test
    void shouldAddMultipleStones() {
        Pit pit = new Pit(3);

        pit.addStones(2);

        assertEquals(5,  pit.getStones());
    }

    @Test
    void shouldReturnAllStonesAndEmptyPit() {
        Pit pit = new Pit(3);

        assertEquals(3, pit.clear());
        assertEquals(0, pit.getStones());
    }
}
