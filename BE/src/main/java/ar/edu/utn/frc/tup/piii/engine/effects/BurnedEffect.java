package ar.edu.utn.frc.tup.piii.engine.effects;

import ar.edu.utn.frc.tup.piii.engine.events.GameEvent;
import ar.edu.utn.frc.tup.piii.engine.events.GameEventPublisher;
import ar.edu.utn.frc.tup.piii.engine.events.GameEventType;
import ar.edu.utn.frc.tup.piii.engine.models.PokemonInPlay;
import ar.edu.utn.frc.tup.piii.enums.SpecialCondition;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Random;

@Component
public class BurnedEffect implements StatusEffectStrategy {

    private static final int BURN_DAMAGE = 20;
    private static final Random RANDOM = new Random();

    @Override
    public SpecialCondition getCondition() {
        return SpecialCondition.BURNED;
    }

    @Override
    public PokemonInPlay applyBetweenTurns(PokemonInPlay pokemon, GameEventPublisher publisher) {
        int newHp = Math.max(0, pokemon.getCurrentHp() - BURN_DAMAGE);
        pokemon.setCurrentHp(newHp);
        publisher.publish(GameEvent.of(GameEventType.DAMAGE_DEALT, pokemon.getPokemon().getName(),
                Map.of("damage", BURN_DAMAGE, "source", "BURN", "pokemon", pokemon.getPokemon().getName())));

        boolean healed = RANDOM.nextBoolean();
        if (healed) {
            pokemon.setSpecialCondition(null);
            publisher.publish(GameEvent.of(GameEventType.STATUS_REMOVED, pokemon.getPokemon().getName(),
                    Map.of("condition", "BURNED", "pokemon", pokemon.getPokemon().getName())));
        }
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
