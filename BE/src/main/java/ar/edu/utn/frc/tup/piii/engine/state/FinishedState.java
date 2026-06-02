package ar.edu.utn.frc.tup.piii.engine.state;

import ar.edu.utn.frc.tup.piii.engine.events.GameEventPublisher;
import ar.edu.utn.frc.tup.piii.engine.models.GameBoard;
import ar.edu.utn.frc.tup.piii.engine.models.TurnContext;
import org.springframework.stereotype.Component;

@Component
public class FinishedState implements MatchState {

    @Override
    public GameBoard enter(GameBoard board, GameEventPublisher publisher) {
        return board;
    }

    @Override
    public GameBoard handle(TurnContext ctx, GameEventPublisher publisher) {
        ctx.getBoard().log("Game is finished, no more actions accepted.");
        return ctx.getBoard();
    }

    @Override
    public String getStateName() {
        return "FINISHED";
    }
}
