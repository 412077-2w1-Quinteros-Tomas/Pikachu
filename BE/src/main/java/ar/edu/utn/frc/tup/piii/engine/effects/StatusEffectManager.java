package ar.edu.utn.frc.tup.piii.engine.effects;

import ar.edu.utn.frc.tup.piii.engine.events.GameEventPublisher;
import ar.edu.utn.frc.tup.piii.engine.models.PlayerBoard;
import ar.edu.utn.frc.tup.piii.engine.models.PokemonInPlay;
import ar.edu.utn.frc.tup.piii.enums.SpecialCondition;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class StatusEffectManager {

    private final List<StatusEffectStrategy> effects;

    public PlayerBoard applyBetweenTurnEffects(PlayerBoard board, GameEventPublisher publisher) {
        PokemonInPlay active = board.getActivePokemon();
        if (active == null || active.getSpecialCondition() == null) return board;

        SpecialCondition condition = active.getSpecialCondition();
        for (StatusEffectStrategy strategy : effects) {
            if (strategy.getCondition().equals(condition)) {
                board.setActivePokemon(strategy.applyBetweenTurns(active, publisher));
                break;
            }
        }
        return board;
    }

    public boolean canAttack(PokemonInPlay pokemon) {
        if (pokemon == null || pokemon.getSpecialCondition() == null) return true;
        SpecialCondition condition = pokemon.getSpecialCondition();
        for (StatusEffectStrategy strategy : effects) {
            if (strategy.getCondition().equals(condition) && strategy.preventsAttack(pokemon)) {
                return false;
            }
        }
        return true;
    }

    public boolean canRetreat(PokemonInPlay pokemon) {
        if (pokemon == null || pokemon.getSpecialCondition() == null) return true;
        SpecialCondition condition = pokemon.getSpecialCondition();
        for (StatusEffectStrategy strategy : effects) {
            if (strategy.getCondition().equals(condition) && strategy.preventsRetreat(pokemon)) {
                return false;
            }
        }
        return true;
    }
}
