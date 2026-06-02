package ar.edu.utn.frc.tup.piii.websocket.messages;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GameActionMessage {

    private String matchId;
    private String playerId;
    /** CONNECT | END_TURN | PLAY_CARD | ATTACH_ENERGY | ATTACK | RETREAT */
    private String actionType;
    private Map<String, Object> payload;
}
