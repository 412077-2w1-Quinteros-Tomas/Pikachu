package ar.edu.utn.frc.tup.piii.engine.models;

import ar.edu.utn.frc.tup.piii.engine.events.GameEvent;
import ar.edu.utn.frc.tup.piii.websocket.messages.GameActionMessage;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TurnContext {

    private GameBoard board;
    private GameActionMessage action;
    private List<GameEvent> events = new ArrayList<>();

    public static TurnContext of(GameBoard board, GameActionMessage action) {
        return new TurnContext(board, action, new ArrayList<>());
    }

    public void addEvent(GameEvent event) {
        events.add(event);
    }
}
