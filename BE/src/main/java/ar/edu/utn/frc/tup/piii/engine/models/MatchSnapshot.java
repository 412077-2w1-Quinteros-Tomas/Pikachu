package ar.edu.utn.frc.tup.piii.engine.models;

import ar.edu.utn.frc.tup.piii.engine.events.GameEvent;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MatchSnapshot {

    private String matchId;
    private GameBoard board;
    private List<GameEvent> events;
    private LocalDateTime timestamp;

    public static MatchSnapshot of(String matchId, GameBoard board, List<GameEvent> events) {
        return new MatchSnapshot(matchId, board, events, LocalDateTime.now());
    }
}
