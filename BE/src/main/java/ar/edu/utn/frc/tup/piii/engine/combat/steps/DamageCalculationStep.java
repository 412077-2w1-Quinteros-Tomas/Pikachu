package ar.edu.utn.frc.tup.piii.engine.combat.steps;

import ar.edu.utn.frc.tup.piii.engine.combat.AttackContext;
import ar.edu.utn.frc.tup.piii.engine.combat.DamageCalculator;
import ar.edu.utn.frc.tup.piii.engine.events.GameEventPublisher;
import ar.edu.utn.frc.tup.piii.engine.models.PokemonCard;
import ar.edu.utn.frc.tup.piii.engine.models.PokemonInPlay;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DamageCalculationStep implements AttackStep {

    private final DamageCalculator damageCalculator;

    @Override
    public AttackContext execute(AttackContext ctx, GameEventPublisher publisher) {
        PokemonInPlay attackerInPlay = ctx.getAttackerBoard().getActivePokemon();
        PokemonInPlay defenderInPlay = ctx.getDefenderBoard().getActivePokemon();

        if (attackerInPlay == null || defenderInPlay == null) return ctx;

        PokemonCard attackerCard = attackerInPlay.getPokemon();
        PokemonCard defenderCard = defenderInPlay.getPokemon();

        int finalDamage = damageCalculator.calculateFinalDamage(
                ctx.getBaseDamage(),
                attackerCard.getTypes(),
                defenderCard.getWeakness(),
                defenderCard.getResistance()
        );

        ctx.setFinalDamage(finalDamage);
        return ctx;
    }
}
