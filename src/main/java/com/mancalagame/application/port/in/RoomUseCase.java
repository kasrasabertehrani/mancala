package com.mancalagame.application.port.in;

import com.mancalagame.domain.model.Player;
import com.mancalagame.domain.model.Room;

public interface RoomUseCase {

    Room createRoom(Player host);

    Room joinRoom(String roomIdStr, Player playerTwo);

    Room getRoom(String roomIdStr);
}
