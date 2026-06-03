package ar.edu.utn.frc.tup.piii.engine.combat.steps;

import ar.edu.utn.frc.tup.piii.engine.combat.AttackContext;
import ar.edu.utn.frc.tup.piii.engine.events.GameEvent;
import ar.edu.utn.frc.tup.piii.engine.events.GameEventPublisher;
import ar.edu.utn.frc.tup.piii.engine.events.GameEventType;
import ar.edu.utn.frc.tup.piii.engine.models.PokemonInPlay;
import ar.edu.utn.frc.tup.piii.enums.SpecialCondition;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class PostDamageEffectStep implements AttackStep {

    @Override
    public AttackContext execute(AttackContext ctx, GameEventPublisher publisher) {
        String effect = ctx.getAttack().getEffect();
        if (effect == null || effect.isBlank()) return ctx;

        PokemonInPlay defender = ctx.getDefenderBoard().getActivePokemon();
        if (defender == null || defender.isKnockedOut()) return ctx;

        String effectUpper = effect.toUpperCase();

        if (effectUpper.contains("POISON")) {
            applyCondition(defender, SpecialCondition.POISONED, ctx, publisher);
        } else if (effectUpper.contains("BURN")) {
            applyCondition(defender, SpecialCondition.BURNED, ctx, publisher);
        } else if (effectUpper.contains("PARALYZ") || effectUpper.contains("PARALYZE")) {
            applyCondition(defender, SpecialCondition.PARALYZED, ctx, publisher);
        } else if (effectUpper.contains("SLEEP") || effectUpper.contains("ASLEEP")) {
            applyCondition(defender, SpecialCondition.ASLEEP, ctx, publisher);
        } else if (effectUpper.contains("CONFUS")) {
            applyCondition(defender, SpecialCondition.CONFUSED, ctx, publisher);
        }

        return ctx;
    }

    private void applyCondition(PokemonInPlay target, SpecialCondition condition,
                                 AttackContext ctx, GameEventPublisher publisher) {
        target.setSpecialCondition(condition);
        publisher.publish(GameEvent.of(GameEventType.STATUS_APPLIED, ctx.getAttackerBoard().getPlayerId(),
                Map.of("condition", condition.name(), "target", target.getPokemon().getName())));
        ctx.getBoard().log(target.getPokemon().getName() + " is now " + condition.name().toLowerCase() + "!");
    }
}
