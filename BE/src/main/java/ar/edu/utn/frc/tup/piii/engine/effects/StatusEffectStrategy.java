package ar.edu.utn.frc.tup.piii.engine.effects;

import ar.edu.utn.frc.tup.piii.engine.events.GameEventPublisher;
import ar.edu.utn.frc.tup.piii.engine.models.PokemonInPlay;
import ar.edu.utn.frc.tup.piii.enums.SpecialCondition;

public interface StatusEffectStrategy {

    SpecialCondition getCondition();

    PokemonInPlay applyBetweenTurns(PokemonInPlay pokemon, GameEventPublisher publisher);

    boolean preventsAttack(PokemonInPlay pokemon);

    boolean preventsRetreat(PokemonInPlay pokemon);
}
