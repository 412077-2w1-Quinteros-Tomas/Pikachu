package ar.edu.utn.frc.tup.piii.engine.rules;

import ar.edu.utn.frc.tup.piii.engine.models.GameBoard;
import ar.edu.utn.frc.tup.piii.engine.models.GameCard;
import ar.edu.utn.frc.tup.piii.engine.models.PlayerBoard;
import ar.edu.utn.frc.tup.piii.engine.models.PokemonCard;
import ar.edu.utn.frc.tup.piii.enums.CardType;
import ar.edu.utn.frc.tup.piii.enums.GamePhase;
import ar.edu.utn.frc.tup.piii.enums.PokemonStage;
import org.springframework.stereotype.Component;

@Component
public class RuleValidator {

    public boolean canPlayCard(GameBoard board, String playerId, String cardId) {
        if (!GamePhase.MAIN.equals(board.getPhase())) return false;
        if (!playerId.equals(board.getCurrentPlayerId())) return false;

        PlayerBoard pb = board.getBoardFor(playerId);
        if (pb == null) return false;

        return pb.getHand().stream()
                .anyMatch(c -> cardId.equals(c.getId())
                        && (c.getCardType() == CardType.POKEMON
                            || c.getCardType() == CardType.TRAINER));
    }

    public boolean canAttachEnergy(GameBoard board, String playerId) {
        if (!GamePhase.MAIN.equals(board.getPhase())) return false;
        if (!playerId.equals(board.getCurrentPlayerId())) return false;

        PlayerBoard pb = board.getBoardFor(playerId);
        if (pb == null || pb.isHasPlayedEnergyThisTurn()) return false;

        return pb.getHand().stream().anyMatch(c -> c.getCardType() == CardType.ENERGY);
    }

    public boolean canAttack(GameBoard board, String playerId, int attackIndex) {
        if (!GamePhase.MAIN.equals(board.getPhase())) return false;
        if (!playerId.equals(board.getCurrentPlayerId())) return false;

        PlayerBoard pb = board.getBoardFor(playerId);
        if (pb == null || pb.isHasAttackedThisTurn() || pb.getActivePokemon() == null) return false;

        PokemonCard pokemon = pb.getActivePokemon().getPokemon();
        return pokemon.getAttacks() != null && attackIndex < pokemon.getAttacks().size();
    }

    public boolean canEndTurn(GameBoard board, String playerId) {
        if (!playerId.equals(board.getCurrentPlayerId())) return false;
        GamePhase phase = board.getPhase();
        return phase == GamePhase.MAIN || phase == GamePhase.BETWEEN_TURNS;
    }

    public boolean canRetreat(GameBoard board, String playerId) {
        if (!GamePhase.MAIN.equals(board.getPhase())) return false;
        if (!playerId.equals(board.getCurrentPlayerId())) return false;

        PlayerBoard pb = board.getBoardFor(playerId);
        return pb != null && !pb.isHasRetreatedThisTurn()
                && pb.getActivePokemon() != null && !pb.getBench().isEmpty();
    }
}
