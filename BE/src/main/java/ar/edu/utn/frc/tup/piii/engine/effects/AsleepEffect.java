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
public class AsleepEffect implements StatusEffectStrategy {

    private static final Random RANDOM = new Random();

    @Override
    public SpecialCondition getCondition() {
        return SpecialCondition.ASLEEP;
    }

    @Override
    public PokemonInPlay applyBetweenTurns(PokemonInPlay pokemon, GameEventPublisher publisher) {
        boolean wakeUp = RANDOM.nextBoolean();
        if (wakeUp) {
            pokemon.setSpecialCondition(null);
            publisher.publish(GameEvent.of(GameEventType.STATUS_REMOVED, pokemon.getPokemon().getName(),
                    Map.of("condition", "ASLEEP", "pokemon", pokemon.getPokemon().getName())));
        }
        return pokemon;
    }

    @Override
    public boolean preventsAttack(PokemonInPlay pokemon) {
        return SpecialCondition.ASLEEP.equals(pokemon.getSpecialCondition());
    }

    @Override
    public boolean preventsRetreat(PokemonInPlay pokemon) {
        return SpecialCondition.ASLEEP.equals(pokemon.getSpecialCondition());
    }
}
