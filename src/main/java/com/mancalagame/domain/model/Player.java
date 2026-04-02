package com.mancalagame.domain.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.mancalagame.domain.model.vo.PlayerId;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;




public class Player {

    private PlayerId id;
    private String name;
    private int stones;



    public Player(String name) {
        this.id = new PlayerId(UUID.randomUUID().toString());
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public PlayerId getId() {
        return id;
    }

    public void setId(PlayerId id) {
        this.id = id;
    }

    public int getStones() {
        return stones;
    }

    public void setStones(int stones) {
        this.stones = stones;
    }

    public void dropStone() {
        if (stones > 0) stones--;
    }
}