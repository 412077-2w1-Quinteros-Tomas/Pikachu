package ar.edu.utn.frc.tup.piii.engine.events;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GameEvent {

    private GameEventType type;
    private String playerId;
    private Map<String, Object> data;
    private LocalDateTime timestamp;

    public static GameEvent of(GameEventType type, String playerId, Map<String, Object> data) {
        return new GameEvent(type, playerId, data, LocalDateTime.now());
    }

    public static GameEvent of(GameEventType type, String playerId) {
        return of(type, playerId, Map.of());
    }
}
