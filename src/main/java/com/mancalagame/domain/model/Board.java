package com.mancalagame.domain.model;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Board {

    static final int TOTAL_PITS = 14;
    static final int PLAYER_1_STORE = 6;
    static final int PLAYER_2_STORE = 13;
    static final int PLAYER_1_PIT_START = 0;
    static final int PLAYER_1_PIT_END = 5;
    static final int PLAYER_2_PIT_START = 7;
    static final int PLAYER_2_PIT_END = 12;
    private static final int INITIAL_STONES = 4;

    private final Pit[] pits;

    public Board() {
        this.pits = new Pit[TOTAL_PITS];
        for (int i = 0; i < TOTAL_PITS; i++) {
            if (i == PLAYER_1_STORE || i == PLAYER_2_STORE) {
                this.pits[i] = new Pit(0);
            } else {
                this.pits[i] = new Pit(INITIAL_STONES);
            }
        }
    }

    public int sowStones(int pitIndex, int opponentStoreIndex) {
        int stonesInHand = pits[pitIndex].clear();
        int currentIndex = pitIndex;

        while (stonesInHand > 0) {
            currentIndex = (currentIndex + 1) % TOTAL_PITS;

            if (currentIndex == opponentStoreIndex) {
                continue;
            }

            pits[currentIndex].increment();
            stonesInHand--;
        }

        return currentIndex;
    }

    public void attemptCapture(boolean isPlayer1, int lastIndex, int myStoreIndex) {
        if (lastIndex == myStoreIndex) return;
        if (lastStoneIsNotCurPlayerSide(isPlayer1, lastIndex)) return;
        if (pits[lastIndex].getStones() != 1) return;

        int oppositeIndex = PLAYER_2_PIT_END - lastIndex;
        if (pits[oppositeIndex].getStones() > 0) {
            int capturedStones = pits[oppositeIndex].clear() + pits[lastIndex].clear();
            pits[myStoreIndex].addStones(capturedStones);
        }
    }

    public void sweepRemainingToStoreOfNonEmptySide(int from, int to, int storeIndex) {
        for (int i = from; i <= to; i++) {
            int remainingStones = pits[i].clear();
            pits[storeIndex].addStones(remainingStones);
        }
    }

    public boolean isSideEmpty(int from, int to) {
        for (int i = from; i <= to; i++) {
            if (pits[i].getStones() > 0) return false;
        }
        return true;
    }

    public boolean lastStoneIsNotCurPlayerSide(boolean isPlayer1, int pitIndex) {
        return isPlayer1 ?
                (pitIndex > PLAYER_1_PIT_END) :
                (pitIndex < PLAYER_2_PIT_START || pitIndex > PLAYER_2_PIT_END);
    }

    public int getStoreIndex(boolean isPlayer1) {
        return isPlayer1 ? PLAYER_1_STORE : PLAYER_2_STORE;
    }

    public int getStonesAt(int index) {
        return pits[index].getStones();
    }

    public int getPlayer1Score() { return pits[PLAYER_1_STORE].getStones(); }

    public int getPlayer2Score() { return pits[PLAYER_2_STORE].getStones(); }
}
