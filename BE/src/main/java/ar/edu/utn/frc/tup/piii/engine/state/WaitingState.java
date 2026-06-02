package ar.edu.utn.frc.tup.piii.engine.state;

import ar.edu.utn.frc.tup.piii.engine.events.GameEvent;
import ar.edu.utn.frc.tup.piii.engine.events.GameEventPublisher;
import ar.edu.utn.frc.tup.piii.engine.events.GameEventType;
import ar.edu.utn.frc.tup.piii.engine.models.GameBoard;
import ar.edu.utn.frc.tup.piii.engine.models.TurnContext;
import ar.edu.utn.frc.tup.piii.enums.GamePhase;
import org.springframework.stereotype.Component;

@Component
public class WaitingState implements MatchState {

    @Override
    public GameBoard enter(GameBoard board, GameEventPublisher publisher) {
        board.setPhase(GamePhase.WAITING);
        return board;
    }

    @Override
    public GameBoard handle(TurnContext ctx, GameEventPublisher publisher) {
        GameBoard board = ctx.getBoard();
        String actionType = ctx.getAction() != null ? ctx.getAction().getActionType() : "";

        if ("CONNECT".equals(actionType)) {
            publisher.publish(GameEvent.of(GameEventType.GAME_STARTED, ctx.getAction().getPlayerId()));
        }

        return board;
    }

    @Override
    public String getStateName() {
        return "WAITING";
    }
}
