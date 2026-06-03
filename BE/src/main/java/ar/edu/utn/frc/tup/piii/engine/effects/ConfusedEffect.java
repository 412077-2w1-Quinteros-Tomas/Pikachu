package ar.edu.utn.frc.tup.piii.engine.effects;

import ar.edu.utn.frc.tup.piii.engine.events.GameEventPublisher;
import ar.edu.utn.frc.tup.piii.engine.models.PokemonInPlay;
import ar.edu.utn.frc.tup.piii.enums.SpecialCondition;
import org.springframework.stereotype.Component;

@Component
public class ConfusedEffect implements StatusEffectStrategy {

    @Override
    public SpecialCondition getCondition() {
        return SpecialCondition.CONFUSED;
    }

    @Override
    public PokemonInPlay applyBetweenTurns(PokemonInPlay pokemon, GameEventPublisher publisher) {
        return pokemon;
    }

    @Override
    public boolean preventsAttack(PokemonInPlay pokemon) {
        return false;
    }

    @Override
    public boolean preventsRetreat(PokemonInPlay pokemon) {
        return false;
    }
}
