package com.mancalagame.domain.model;

import com.mancalagame.domain.model.vo.PlayerId;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class Player {

    private PlayerId id;
    private String name;
    private int stones;

    public Player(String name) {
        this.id = new PlayerId(UUID.randomUUID().toString());
        this.name = name;
    }
}