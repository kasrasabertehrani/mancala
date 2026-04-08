package com.mancalagame.application.port.in;

import com.mancalagame.domain.model.Room;
import com.mancalagame.domain.model.vo.PlayerId;
import com.mancalagame.domain.model.vo.RoomId;

import java.util.List;

public interface GameUseCase {

    Room makeMove(RoomId roomId, PlayerId playerId, int pitIndex);

    Room handlePlayerDisconnect(RoomId roomId, PlayerId playerId);

    Room handlePlayerReconnect(RoomId roomId, PlayerId playerId);

    List<Room> processTimeouts();
}