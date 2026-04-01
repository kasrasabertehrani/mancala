package com.mancalagame.domain.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class BoardTest {

    @Test
    void shouldSowStones() {
        Board board = new Board();

        board.sowStones(0, 13);

        assertEquals(0, board.getPits()[0].getStones());
        assertEquals(5, board.getPits()[1].getStones());
        assertEquals(5, board.getPits()[2].getStones());
        assertEquals(5, board.getPits()[3].getStones());
        assertEquals(5, board.getPits()[4].getStones());
    }

    @Test
    void shouldSkipOpponentStoreWhenSow() {
        Board board = new Board();
        board.getPits()[5].setStones(10);

        board.sowStones(5, 13);

        assertEquals(0, board.getPits()[13].getStones());
        assertEquals(5, board.getPits()[0].getStones());
        assertEquals(5, board.getPits()[1].getStones());
    }

    @Test
    void shouldReturnTrueWhenSideIsEmpty() {
        Board board = new Board();
        board.getPits()[0].setStones(0);
        board.getPits()[1].setStones(0);
        board.getPits()[2].setStones(0);
        board.getPits()[3].setStones(0);
        board.getPits()[4].setStones(0);
        board.getPits()[5].setStones(0);

        assertTrue(board.isSideEmpty(0, 5));
    }

    @Test
    void shouldReturnFalseWhenSideNotEmpty() {
        Board board = new Board();

        assertFalse(board.isSideEmpty(0, 5));
    }

    @Test
    void shouldSweepRemainingStonesToNonEmptySideStore() {
        Board board = new Board();

        board.sweepRemainingToStoreOfNonEmptySide(0, 5, 6);

        assertEquals(0, board.getPits()[0].getStones());
        assertEquals(0, board.getPits()[1].getStones());
        assertEquals(0, board.getPits()[2].getStones());
        assertEquals(0, board.getPits()[3].getStones());
        assertEquals(0, board.getPits()[4].getStones());
        assertEquals(0, board.getPits()[5].getStones());
        assertEquals(24, board.getPits()[6].getStones());
    }

    @Test
    void shouldReturnFalseWhenPitIsOnPlayer1Side() {
        Board board = new Board();

        assertFalse(board.lastStoneIsNotCurPlayerSide(true, 0));
        assertFalse(board.lastStoneIsNotCurPlayerSide(true, 3));
        assertFalse(board.lastStoneIsNotCurPlayerSide(true, 5));
    }

    @Test
    void shouldReturnTrueWhenPitIsNotOnPlayer1Side() {
        Board board = new Board();

        assertTrue(board.lastStoneIsNotCurPlayerSide(true, 7));
        assertTrue(board.lastStoneIsNotCurPlayerSide(true, 10));
        assertTrue(board.lastStoneIsNotCurPlayerSide(true, 12));
    }

    @Test
    void shouldReturnFalseWhenPitIsOnPlayer2Side() {
        Board board = new Board();

        assertTrue(board.lastStoneIsNotCurPlayerSide(false, 0));
        assertTrue(board.lastStoneIsNotCurPlayerSide(false, 3));
        assertTrue(board.lastStoneIsNotCurPlayerSide(false, 5));
    }

    @Test
    void shouldReturnTrueWhenPitIsNotOnPlayer2Side() {
        Board board = new Board();

        assertFalse(board.lastStoneIsNotCurPlayerSide(false, 7));
        assertFalse(board.lastStoneIsNotCurPlayerSide(false, 10));
        assertFalse(board.lastStoneIsNotCurPlayerSide(false, 12));
    }

    @Test
    void shouldReturnFalseWhenPitIsStoreOfPlayerTwo() {
        Board board = new Board();

        assertTrue(board.lastStoneIsNotCurPlayerSide(false, 13));
    }

    @Test
    void shouldReturnPlayer1Score() {
        Board board = new Board();

        board.sowStones(5, 13);

        assertEquals(1,  board.getPlayer1Score());
    }

    @Test
    void shouldReturnPlayer2Score() {
        Board board = new Board();

        board.sowStones(12, 6);

        assertEquals(1,  board.getPlayer2Score());
    }
}