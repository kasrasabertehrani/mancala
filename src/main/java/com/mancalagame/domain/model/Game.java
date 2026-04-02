package com.mancalagame.domain.model;

import com.mancalagame.domain.exception.InvalidGameStateException;
import com.mancalagame.domain.exception.InvalidPlayerException;
import com.mancalagame.domain.model.vo.PlayerId;



public class Game {

    public enum GameStatus {
        WAITING_FOR_PLAYER_2,
        PLAYER_1_TURN,
        PLAYER_2_TURN,
        GAME_OVER,
        FORFEIT,
        MATCH_SUSPENDED
    }

    private final Player player1;
    private Player player2;
    private final Board board;
    private GameStatus gameStatus;
    private PlayerId absentPlayerId;
    private GameStatus previousStatus;

    public record MoveResult(boolean wasCapture, boolean wasFreeTurn) {}

    public Game(Player player1) {
        this.player1 = player1;
        this.board = new Board();
        this.gameStatus = GameStatus.WAITING_FOR_PLAYER_2;
    }

    public void setPlayer2(Player player2) {
        this.player2 = player2;
        this.gameStatus = GameStatus.PLAYER_1_TURN;
    }

    private void switchTurn() {
        if (this.gameStatus == GameStatus.PLAYER_1_TURN) {
            this.gameStatus = GameStatus.PLAYER_2_TURN;
        } else if (this.gameStatus == GameStatus.PLAYER_2_TURN) {
            this.gameStatus = GameStatus.PLAYER_1_TURN;
        }
    }

    private void endGame() {
        this.gameStatus = GameStatus.GAME_OVER;
        board.sweepRemainingToStoreOfNonEmptySide(Board.PLAYER_1_PIT_START, Board.PLAYER_1_PIT_END, Board.PLAYER_1_STORE);
        board.sweepRemainingToStoreOfNonEmptySide(Board.PLAYER_2_PIT_START, Board.PLAYER_2_PIT_END, Board.PLAYER_2_STORE);
    }

    public MoveResult playTurn(PlayerId playerId, int pitIndex) {
        validateMove(playerId, pitIndex);

        boolean isPlayer1 = isPlayer1(playerId);
        int myStoreIndex = board.getStoreIndex(isPlayer1);
        int opponentStoreIndex = board.getStoreIndex(!isPlayer1);

        int lastIndex = board.sowStones(pitIndex, opponentStoreIndex);

        boolean lastMoveCaptured = board.attemptCapture(isPlayer1, lastIndex, myStoreIndex);


        boolean lastMoveGrantedFreeTurn = (lastIndex == myStoreIndex);

        if (!lastMoveGrantedFreeTurn) {
            switchTurn();
        }

        checkGameOver();
        return new MoveResult(lastMoveCaptured, lastMoveGrantedFreeTurn);
    }

    private void checkGameOver() {
        boolean p1Empty = board.isSideEmpty(Board.PLAYER_1_PIT_START, Board.PLAYER_1_PIT_END);
        boolean p2Empty = board.isSideEmpty(Board.PLAYER_2_PIT_START, Board.PLAYER_2_PIT_END);

        if (p1Empty || p2Empty) {
            endGame();
        }
    }

    public void markPlayerAbsent(PlayerId playerId) {
        if (player2 == null) {
            this.gameStatus = GameStatus.GAME_OVER;
            return;
        }
        this.previousStatus = this.gameStatus;
        this.gameStatus = GameStatus.MATCH_SUSPENDED;
        this.absentPlayerId = playerId;
    }

    public void markPlayerReturned(PlayerId playerId) {
        if (gameStatus != GameStatus.MATCH_SUSPENDED) return;
        if (!playerId.equals(absentPlayerId)) return;

        this.gameStatus = this.previousStatus;
        this.absentPlayerId = null;
        this.previousStatus = null;
    }

    public void forfeit(PlayerId playerId) {
        this.gameStatus = GameStatus.FORFEIT;
        this.absentPlayerId = playerId;
    }

    // We still return String here so "DRAW" works cleanly with your DTOs
    public String getWinner() {
        if (gameStatus == GameStatus.FORFEIT) {
            return isPlayer1(absentPlayerId) ? player2.getId().value() : player1.getId().value();
        }
        if (gameStatus != GameStatus.GAME_OVER) return null;

        int p1Score = board.getPlayer1Score();
        int p2Score = board.getPlayer2Score();

        if (p1Score > p2Score) return player1.getId().value();
        else if (p2Score > p1Score) return player2.getId().value();
        else return "DRAW";
    }

    private void validateMove(PlayerId playerId, int pitIndex) {
        if (playerId == null || (!isPlayer1(playerId) && (player2 == null || !player2.getId().equals(playerId)))) {
            String idVal = (playerId != null) ? playerId.value() : "null";
            throw new InvalidPlayerException(idVal, "Unknown player attempted a move.");
        }

        boolean isPlayer1 = isPlayer1(playerId);

        if (
                this.gameStatus == GameStatus.WAITING_FOR_PLAYER_2 ||
                this.gameStatus == GameStatus.GAME_OVER ||
                this.gameStatus == GameStatus.FORFEIT ||
                this.gameStatus == GameStatus.MATCH_SUSPENDED
        ) {
            throw new InvalidGameStateException("Game is not in a playable state.");
        }

        if (isPlayer1 && this.gameStatus != GameStatus.PLAYER_1_TURN) {
            throw new InvalidGameStateException("Not Player 1's turn!");
        }

        if (!isPlayer1 && this.gameStatus != GameStatus.PLAYER_2_TURN) {
            throw new InvalidGameStateException("Not Player 2's turn!");
        }

        if (board.lastStoneIsNotCurPlayerSide(isPlayer1, pitIndex)) {
            throw new InvalidGameStateException(isPlayer1 ? "Player 1 can only pick pits 0-5." : "Player 2 can only pick pits 7-12.");
        }

        if (board.getStonesAt(pitIndex) == 0) {
            throw new InvalidGameStateException("Cannot pick an empty pit.");
        }
    }

    private boolean isPlayer1(PlayerId playerId) {
        return this.player1.getId().equals(playerId);
    }

    public int getPlayer1Score() { return board.getPlayer1Score(); }
    public int getPlayer2Score() { return board.getPlayer2Score(); } // check later


    public Player getPlayer1() { return player1; }
    public Player getPlayer2() { return player2; }
    public Board getBoard() { return board; }
    public GameStatus getGameStatus() { return gameStatus; }
    public PlayerId getAbsentPlayerId() { return absentPlayerId; }

}