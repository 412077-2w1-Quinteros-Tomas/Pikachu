package ar.edu.utn.frc.tup.piii.engine.effects;

import ar.edu.utn.frc.tup.piii.engine.events.GameEvent;
import ar.edu.utn.frc.tup.piii.engine.events.GameEventPublisher;
import ar.edu.utn.frc.tup.piii.engine.events.GameEventType;
import ar.edu.utn.frc.tup.piii.engine.models.PokemonInPlay;
import ar.edu.utn.frc.tup.piii.enums.SpecialCondition;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class PoisonedEffect implements StatusEffectStrategy {

    private static final int POISON_DAMAGE = 10;

    @Override
    public SpecialCondition getCondition() {
        return SpecialCondition.POISONED;
    }

    @Override
    public PokemonInPlay applyBetweenTurns(PokemonInPlay pokemon, GameEventPublisher publisher) {
        int newHp = Math.max(0, pokemon.getCurrentHp() - POISON_DAMAGE);
        pokemon.setCurrentHp(newHp);
        publisher.publish(GameEvent.of(GameEventType.DAMAGE_DEALT, pokemon.getPokemon().getName(),
                Map.of("damage", POISON_DAMAGE, "source", "POISON", "pokemon", pokemon.getPokemon().getName())));
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
