package com.mancalagame.domain.model;

import com.mancalagame.domain.exception.InvalidGameStateException;
import com.mancalagame.domain.exception.InvalidPlayerException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class GameTest {

    @Test
    void shouldSwitchTurnPlayer2() {
        Player playerOne = new Player("Alice");
        Player playerTwo = new Player("Bob");
        Game game = new Game(playerOne);
        game.setPlayer2(playerTwo);

        game.playTurn(playerOne.getId(), 5);

        assertEquals(Game.GameStatus.PLAYER_2_TURN, game.getGameStatus());
    }

    @Test
    void shouldEndGameWhenPlayerOneSideEmpty() {
        Player playerOne = new Player("Alice");
        Player playerTwo = new Player("Bob");
        Game game = new Game(playerOne);
        game.setPlayer2(playerTwo);
        game.getBoard().getPits()[0].setStones(0);
        game.getBoard().getPits()[1].setStones(0);
        game.getBoard().getPits()[2].setStones(0);
        game.getBoard().getPits()[3].setStones(0);
        game.getBoard().getPits()[4].setStones(0);
        game.getBoard().getPits()[5].setStones(1);

        game.playTurn(playerOne.getId(), 5);

        assertEquals(Game.GameStatus.GAME_OVER, game.getGameStatus());
    }

    @Test
    void shouldEndGameWhenPlayerTwoSideEmpty() {
        Player playerOne = new Player("Alice");
        Player playerTwo = new Player("Bob");
        Game game = new Game(playerOne);
        game.setPlayer2(playerTwo);
        game.getBoard().getPits()[4].setStones(1);
        game.getBoard().getPits()[5].setStones(0);

        game.getBoard().getPits()[7].setStones(0);
        game.getBoard().getPits()[8].setStones(0);
        game.getBoard().getPits()[9].setStones(0);
        game.getBoard().getPits()[10].setStones(0);
        game.getBoard().getPits()[11].setStones(0);
        game.getBoard().getPits()[12].setStones(1);

        game.playTurn(playerOne.getId(), 4);
        game.playTurn(playerTwo.getId(), 12);

        assertEquals(Game.GameStatus.GAME_OVER, game.getGameStatus());
    }


    @Test
    void shouldSwitchTurnsTwice() {
        Player playerOne = new Player("Alice");
        Player playerTwo = new Player("Bob");
        Game game = new Game(playerOne);
        game.setPlayer2(playerTwo);

        assertEquals(Game.GameStatus.PLAYER_1_TURN, game.getGameStatus());
        game.playTurn(playerOne.getId(), 0);
        assertEquals(Game.GameStatus.PLAYER_2_TURN, game.getGameStatus());
        game.playTurn(playerTwo.getId(), 7);
        assertEquals(Game.GameStatus.PLAYER_1_TURN, game.getGameStatus());
    }

    @Test
    void absentPlayerOneShouldLose() {
        Player playerOne = new Player("Alice");
        Player playerTwo = new Player("Bob");
        Game game = new Game(playerOne);
        game.setPlayer2(playerTwo);

        game.forfeit(playerOne.getId());

        assertTrue(playerTwo.getId().toString().contains(game.getWinner()));
    }

    @Test
    void absentPlayerTwoShouldLose() {
        Player playerOne = new Player("Alice");
        Player playerTwo = new Player("Bob");
        Game game = new Game(playerOne);
        game.setPlayer2(playerTwo);

        game.forfeit(playerTwo.getId());

        assertTrue(playerOne.getId().toString().contains(game.getWinner()));
    }

    @Test
    void shouldReturnNullIfGameStatusIsNotGameOver() {
        Player playerOne = new Player("Alice");
        Player playerTwo = new Player("Bob");
        Game game = new Game(playerOne);
        game.setPlayer2(playerTwo);

        assertNull(game.getWinner());
    }

    @Test
    void shouldReturnPlayerOneWhenHeHasMoreStones() { //should it be this way?
        Player playerOne = new Player("Alice");
        Player playerTwo = new Player("Bob");
        Game game = new Game(playerOne);
        game.setPlayer2(playerTwo);

        game.getBoard().getPits()[7].setStones(1);
        game.getBoard().getPits()[8].setStones(1);
        game.getBoard().getPits()[9].setStones(1);
        game.getBoard().getPits()[10].setStones(1);
        game.getBoard().getPits()[11].setStones(1);
        game.getBoard().getPits()[12].setStones(1);
        game.getBoard().getPits()[13].setStones(0);

        game.getBoard().getPits()[0].setStones(0);
        game.getBoard().getPits()[1].setStones(0);
        game.getBoard().getPits()[2].setStones(0);
        game.getBoard().getPits()[3].setStones(0);
        game.getBoard().getPits()[4].setStones(0);
        game.getBoard().getPits()[5].setStones(1);
        game.getBoard().getPits()[6].setStones(23);
        game.playTurn(playerOne.getId(), 5);

        assertTrue(playerOne.getId().toString().contains(game.getWinner()));
    }

    @Test
    void shouldReturnPlayerTwoWhenHeHasMoreStones() { //should it be this way?
        Player playerOne = new Player("Alice");
        Player playerTwo = new Player("Bob");
        Game game = new Game(playerOne);
        game.setPlayer2(playerTwo);

        game.getBoard().getPits()[0].setStones(1);
        game.getBoard().getPits()[1].setStones(1);
        game.getBoard().getPits()[2].setStones(1);
        game.getBoard().getPits()[3].setStones(1);
        game.getBoard().getPits()[4].setStones(1);
        game.getBoard().getPits()[5].setStones(1);
        game.getBoard().getPits()[6].setStones(0);

        game.playTurn(playerOne.getId(), 0);

        game.getBoard().getPits()[7].setStones(0);
        game.getBoard().getPits()[8].setStones(0);
        game.getBoard().getPits()[9].setStones(0);
        game.getBoard().getPits()[10].setStones(0);
        game.getBoard().getPits()[11].setStones(0);
        game.getBoard().getPits()[12].setStones(1);
        game.getBoard().getPits()[13].setStones(23);
        game.playTurn(playerTwo.getId(), 12);

        assertTrue(playerTwo.getId().toString().contains(game.getWinner()));
    }

    @Test
    void shouldReturnDrawWhenEqualScore() { //should it be this way?
        Player playerOne = new Player("Alice");
        Player playerTwo = new Player("Bob");
        Game game = new Game(playerOne);
        game.setPlayer2(playerTwo);

        game.getBoard().getPits()[0].setStones(0);
        game.getBoard().getPits()[1].setStones(0);
        game.getBoard().getPits()[2].setStones(0);
        game.getBoard().getPits()[3].setStones(0);
        game.getBoard().getPits()[4].setStones(0);
        game.getBoard().getPits()[5].setStones(1);
        game.getBoard().getPits()[6].setStones(23);
        game.playTurn(playerOne.getId(), 5);

        assertEquals("DRAW", game.getWinner());
    }

    @Test
    void shouldThrowExceptionWhenWaitingForPlayer() {
        Player playerOne = new Player("Alice");
        Game game = new Game(playerOne);

        assertThrows(InvalidGameStateException.class, () -> game.playTurn(playerOne.getId(), 0));
    }

    @Test
    void shouldThrowExceptionWhenGameOverAndPlayTurn() {
        Player playerOne = new Player("Alice");
        Player playerTwo = new Player("Bob");
        Game game = new Game(playerOne);
        game.setPlayer2(playerTwo); // maybe join?

        game.getBoard().getPits()[0].setStones(0);
        game.getBoard().getPits()[1].setStones(0);
        game.getBoard().getPits()[2].setStones(0);
        game.getBoard().getPits()[3].setStones(0);
        game.getBoard().getPits()[4].setStones(0);
        game.getBoard().getPits()[5].setStones(1);
        game.getBoard().getPits()[6].setStones(23);
        game.playTurn(playerOne.getId(), 5);

        assertThrows(InvalidGameStateException.class, () -> game.playTurn(playerOne.getId(), 0));
    }

    @Test
    void shouldThrowWhenPlayTurnAndStatusForfeit() {
        Player playerOne = new Player("Alice");
        Player playerTwo = new Player("Bob");
        Game game = new Game(playerOne);
        game.setPlayer2(playerTwo);
        game.forfeit(playerOne.getId());

        assertThrows(InvalidGameStateException.class, () -> game.playTurn(playerOne.getId(), 0));
    }

    @Test
    void shouldThrowWhenPlayTurnAndMatchSuspended() {
        Player playerOne = new Player("Alice");
        Player playerTwo = new Player("Bob");
        Game game = new Game(playerOne);
        game.setPlayer2(playerTwo);
        game.markPlayerAbsent(playerTwo.getId());

        assertThrows(InvalidGameStateException.class, () -> game.playTurn(playerOne.getId(), 0));
    }

    @Test
    void shouldThrowWhenPlayerTwoTurnAndPlayerOnePlayTurn() {
        Player playerOne = new Player("Alice");
        Player playerTwo = new Player("Bob");
        Game game = new Game(playerOne);
        game.setPlayer2(playerTwo);
        game.playTurn(playerOne.getId(), 0);

        assertThrows(InvalidGameStateException.class, () -> game.playTurn(playerOne.getId(), 1));
    }

    @Test
    void shouldThrowWhenPlayerOneTurnAndPlayerTwoPlayTurn() {
        Player playerOne = new Player("Alice");
        Player playerTwo = new Player("Bob");
        Game game = new Game(playerOne);
        game.setPlayer2(playerTwo);
        game.playTurn(playerOne.getId(), 0);
        game.playTurn(playerTwo.getId(), 7);

        assertThrows(InvalidGameStateException.class, () -> game.playTurn(playerTwo.getId(), 8));
    }

    @Test
    void shouldThrowWhenPlayerOneChoosePitOfPlayerTwo() {
        Player playerOne = new Player("Alice");
        Player playerTwo = new Player("Bob");
        Game game = new Game(playerOne);
        game.setPlayer2(playerTwo);

        assertThrows(InvalidGameStateException.class, () -> game.playTurn(playerOne.getId(), 7));
    }

    @Test
    void shouldThrowWhenPlayerTwoChoosePitOfPlayerOne() {
        Player playerOne = new Player("Alice");
        Player playerTwo = new Player("Bob");
        Game game = new Game(playerOne);
        game.setPlayer2(playerTwo);
        game.playTurn(playerOne.getId(), 0);

        assertThrows(InvalidGameStateException.class, () -> game.playTurn(playerTwo.getId(), 0));
    }

    @Test
    void shouldThrowWhenNullPlayerPlayTurn() {
        Player playerOne = new Player("Alice");
        Player playerTwo = new Player("Bob");
        Game game = new Game(playerOne);
        game.setPlayer2(playerTwo);

        assertThrows(InvalidPlayerException.class, () -> game.playTurn(null, 0));
    }

    @Test
    void shouldThrowWhenUnknownPlayerPlayTurn() {
        Player playerOne = new Player("Alice");
        Player playerTwo = new Player("Bob");
        Player playerThree = new Player("Charlie");
        Game game = new Game(playerOne);
        game.setPlayer2(playerTwo);

        assertThrows(InvalidPlayerException.class, () -> game.playTurn(playerThree.getId(), 0));
    }

    @Test
    void shouldThrowExceptionWhenNotPlayerOneAndNotPlayerTwoPlayTurn() {
        Player playerOne = new Player("Alice");
        Player playerTwo = new Player("Bob");
        Player playerThree = new Player("Charlie");
        Game game = new Game(playerOne);
        game.setPlayer2(playerTwo);
        game.playTurn(playerOne.getId(), 0);

        assertThrows(InvalidPlayerException.class, () -> game.playTurn(playerThree.getId(), 0));
    }

    @Test
    void shouldThrowExceptionWhenNotPlayerAndPlayerTwoIsNullPlayTurn() {
        Player playerOne = new Player("Alice");
        Player playerTwo = new Player("Bob");
        Game game = new Game(playerOne);
        game.setPlayer2(playerTwo);
        game.playTurn(playerOne.getId(), 0);

        assertThrows(InvalidPlayerException.class, () -> game.playTurn(null, 0));
    }

    @Test
    void shouldReturnFalseWhenMatchNotSuspended() {
        Player playerOne = new Player("Alice");
        Player playerTwo = new Player("Bob");
        Game game = new Game(playerOne);
        game.setPlayer2(playerTwo);

        assertFalse(game.markPlayerReturned(playerOne.getId()));
    }

    @Test
    void shouldReturnFalseWhenPlayerNotAbsent() {
        Player playerOne = new Player("Alice");
        Player playerTwo = new Player("Bob");
        Game game = new Game(playerOne);
        game.setPlayer2(playerTwo);
        game.markPlayerAbsent(playerTwo.getId());

        assertFalse(game.markPlayerReturned(playerOne.getId()));
    }

    @Test
    void shouldThrowExceptionWhenPickEmptyPit() {
        Player playerOne = new Player("Alice");
        Player playerTwo = new Player("Bob");
        Game game = new Game(playerOne);
        game.setPlayer2(playerTwo);
        game.getBoard().getPits()[0].setStones(0);

        assertThrows(InvalidGameStateException.class, () -> game.playTurn(playerOne.getId(), 0));
    }
}