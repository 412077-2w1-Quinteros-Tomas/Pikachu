package ar.edu.utn.frc.tup.piii.websocket.messages;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GameStateUpdateMessage {

    /** PLAYER_CONNECTED | PLAYER_DISCONNECTED | GAME_STARTED | STATE_UPDATE | GAME_OVER | ERROR */
    private String type;
    private String matchId;
    private Object data;
}
