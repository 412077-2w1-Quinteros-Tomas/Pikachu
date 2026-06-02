package ar.edu.utn.frc.tup.piii.engine.rules;

import ar.edu.utn.frc.tup.piii.engine.events.GameEvent;
import ar.edu.utn.frc.tup.piii.engine.events.GameEventPublisher;
import ar.edu.utn.frc.tup.piii.engine.events.GameEventType;
import ar.edu.utn.frc.tup.piii.engine.models.GameBoard;
import ar.edu.utn.frc.tup.piii.engine.models.GameCard;
import ar.edu.utn.frc.tup.piii.engine.models.PlayerBoard;
import ar.edu.utn.frc.tup.piii.enums.GamePhase;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class TurnManager {

    public GameBoard drawCard(GameBoard board, GameEventPublisher publisher) {
        PlayerBoard current = board.getBoardFor(board.getCurrentPlayerId());
        if (current == null) return board;

        if (current.getDeck().isEmpty()) {
            board.log(current.getPlayerId() + " has no cards to draw — they lose!");
            board.setWinnerId(board.getOpponentBoard(current.getPlayerId()).getPlayerId());
            board.setPhase(GamePhase.FINISHED);
            return board;
        }

        GameCard drawn = current.getDeck().remove(0);
        current.getHand().add(drawn);

        publisher.publish(GameEvent.of(GameEventType.CARD_DRAWN, current.getPlayerId(),
                Map.of("deckSize", current.getDeck().size())));

        board.setPhase(GamePhase.MAIN);
        return board;
    }

    public GameBoard endTurn(GameBoard board, GameEventPublisher publisher) {
        String currentPlayerId = board.getCurrentPlayerId();
        PlayerBoard current = board.getBoardFor(currentPlayerId);
        if (current != null) current.resetTurnFlags();

        publisher.publish(GameEvent.of(GameEventType.TURN_ENDED, currentPlayerId,
                Map.of("turnNumber", board.getTurnNumber())));

        String nextPlayerId = board.getPlayer1Board().getPlayerId().equals(currentPlayerId)
                ? board.getPlayer2Board().getPlayerId()
                : board.getPlayer1Board().getPlayerId();

        board.setCurrentPlayerId(nextPlayerId);
        board.setTurnNumber(board.getTurnNumber() + 1);

        publisher.publish(GameEvent.of(GameEventType.TURN_STARTED, nextPlayerId,
                Map.of("turnNumber", board.getTurnNumber())));

        return drawCard(board, publisher);
    }
}
