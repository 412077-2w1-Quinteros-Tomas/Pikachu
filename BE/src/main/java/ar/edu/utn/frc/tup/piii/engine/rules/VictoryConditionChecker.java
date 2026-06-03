package ar.edu.utn.frc.tup.piii.engine.rules;

import ar.edu.utn.frc.tup.piii.engine.models.GameBoard;
import ar.edu.utn.frc.tup.piii.engine.models.PlayerBoard;
import ar.edu.utn.frc.tup.piii.enums.GamePhase;
import org.springframework.stereotype.Component;

@Component
public class VictoryConditionChecker {

    /**
     * Returns the winner's playerId, or null if the game is not over.
     * Victory conditions:
     * 1. A player takes all their prize cards
     * 2. A player's opponent has no active Pokémon and an empty bench
     * 3. A player's opponent must draw but has no cards
     */
    public String check(GameBoard board) {
        if (board.isFinished()) return board.getWinnerId();

        PlayerBoard p1 = board.getPlayer1Board();
        PlayerBoard p2 = board.getPlayer2Board();
        if (p1 == null || p2 == null) return null;

        // All prize cards taken
        if (p1.getPrizeCards().isEmpty() && board.getTurnNumber() > 1) return p1.getPlayerId();
        if (p2.getPrizeCards().isEmpty() && board.getTurnNumber() > 1) return p2.getPlayerId();

        // Opponent has no Pokémon (guard: turn > 1 prevents false wins during setup)
        if (board.getTurnNumber() > 1) {
            if (p1.hasNoPokemon()) return p2.getPlayerId();
            if (p2.hasNoPokemon()) return p1.getPlayerId();
        }

        return null;
    }
}
