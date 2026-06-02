package ar.edu.utn.frc.tup.piii.engine.state;

import ar.edu.utn.frc.tup.piii.engine.events.GameEventPublisher;
import ar.edu.utn.frc.tup.piii.engine.models.GameBoard;
import ar.edu.utn.frc.tup.piii.engine.models.TurnContext;

public interface MatchState {

    /** Called when entering this state. Returns the updated board. */
    GameBoard enter(GameBoard board, GameEventPublisher publisher);

    /** Handles a player action. Returns the updated board. */
    GameBoard handle(TurnContext ctx, GameEventPublisher publisher);

    String getStateName();
}
