package ar.edu.utn.frc.tup.piii.engine.effects;

import ar.edu.utn.frc.tup.piii.engine.events.GameEvent;
import ar.edu.utn.frc.tup.piii.engine.events.GameEventPublisher;
import ar.edu.utn.frc.tup.piii.engine.events.GameEventType;
import ar.edu.utn.frc.tup.piii.engine.models.PokemonInPlay;
import ar.edu.utn.frc.tup.piii.enums.SpecialCondition;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class ParalyzedEffect implements StatusEffectStrategy {

    @Override
    public SpecialCondition getCondition() {
        return SpecialCondition.PARALYZED;
    }

    @Override
    public PokemonInPlay applyBetweenTurns(PokemonInPlay pokemon, GameEventPublisher publisher) {
        pokemon.setSpecialCondition(null);
        publisher.publish(GameEvent.of(GameEventType.STATUS_REMOVED, pokemon.getPokemon().getName(),
                Map.of("condition", "PARALYZED", "pokemon", pokemon.getPokemon().getName())));
        return pokemon;
    }

    @Override
    public boolean preventsAttack(PokemonInPlay pokemon) {
        return SpecialCondition.PARALYZED.equals(pokemon.getSpecialCondition());
    }

    @Override
    public boolean preventsRetreat(PokemonInPlay pokemon) {
        return SpecialCondition.PARALYZED.equals(pokemon.getSpecialCondition());
    }
}
