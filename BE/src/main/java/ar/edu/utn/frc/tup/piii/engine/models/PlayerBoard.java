package ar.edu.utn.frc.tup.piii.engine.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PlayerBoard {

    public static final int MAX_BENCH_SIZE = 5;
    public static final int PRIZE_CARD_COUNT = 6;

    private String playerId;
    private List<GameCard> hand = new ArrayList<>();
    private List<GameCard> deck = new ArrayList<>();
    private List<GameCard> discardPile = new ArrayList<>();
    private PokemonInPlay activePokemon;
    private List<PokemonInPlay> bench = new ArrayList<>();
    private List<GameCard> prizeCards = new ArrayList<>();
    private boolean hasPlayedEnergyThisTurn;
    private boolean hasAttackedThisTurn;
    private boolean hasRetreatedThisTurn;

    public boolean hasBenchSpace() {
        return bench == null || bench.size() < MAX_BENCH_SIZE;
    }

    public boolean hasNoPokemon() {
        return activePokemon == null
                && (bench == null || bench.stream().noneMatch(p -> !p.isKnockedOut()));
    }

    public void resetTurnFlags() {
        hasPlayedEnergyThisTurn = false;
        hasAttackedThisTurn = false;
        hasRetreatedThisTurn = false;
    }
}
