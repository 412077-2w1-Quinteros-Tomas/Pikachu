package ar.edu.utn.frc.tup.piii.engine.state;

import ar.edu.utn.frc.tup.piii.engine.events.GameEvent;
import ar.edu.utn.frc.tup.piii.engine.events.GameEventPublisher;
import ar.edu.utn.frc.tup.piii.engine.events.GameEventType;
import ar.edu.utn.frc.tup.piii.engine.models.GameBoard;
import ar.edu.utn.frc.tup.piii.engine.models.PlayerBoard;
import ar.edu.utn.frc.tup.piii.engine.models.PokemonCard;
import ar.edu.utn.frc.tup.piii.engine.models.PokemonInPlay;
import ar.edu.utn.frc.tup.piii.engine.models.TurnContext;
import ar.edu.utn.frc.tup.piii.enums.GamePhase;
import ar.edu.utn.frc.tup.piii.enums.PokemonStage;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class SetupState implements MatchState {

    @Override
    public GameBoard enter(GameBoard board, GameEventPublisher publisher) {
        board.setPhase(GamePhase.SETUP);
        autoSetupPlayer(board.getPlayer1Board());
        autoSetupPlayer(board.getPlayer2Board());

        publisher.publish(GameEvent.of(GameEventType.SETUP_COMPLETE, "system",
                Map.of("matchId", board.getMatchId())));

        return board;
    }

    private void autoSetupPlayer(PlayerBoard playerBoard) {
        if (playerBoard == null) return;

        // Place first available Basic Pokemon as active
        if (playerBoard.getActivePokemon() == null) {
            for (int i = 0; i < playerBoard.getHand().size(); i++) {
                if (playerBoard.getHand().get(i) instanceof PokemonCard pokemon
                        && pokemon.getStage() == PokemonStage.BASIC) {
                    playerBoard.getHand().remove(i);
                    playerBoard.setActivePokemon(PokemonInPlay.of(pokemon));
                    break;
                }
            }
        }
    }

    @Override
    public GameBoard handle(TurnContext ctx, GameEventPublisher publisher) {
        return ctx.getBoard();
    }

    @Override
    public String getStateName() {
        return "SETUP";
    }
}
