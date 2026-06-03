package ar.edu.utn.frc.tup.piii.engine.combat.steps;

import ar.edu.utn.frc.tup.piii.engine.combat.AttackContext;
import ar.edu.utn.frc.tup.piii.engine.events.GameEvent;
import ar.edu.utn.frc.tup.piii.engine.events.GameEventPublisher;
import ar.edu.utn.frc.tup.piii.engine.events.GameEventType;
import ar.edu.utn.frc.tup.piii.engine.models.PokemonInPlay;
import ar.edu.utn.frc.tup.piii.enums.SpecialCondition;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Random;

@Component
public class PreAttackEffectStep implements AttackStep {

    private static final Random RANDOM = new Random();

    @Override
    public AttackContext execute(AttackContext ctx, GameEventPublisher publisher) {
        PokemonInPlay attacker = ctx.getAttackerBoard().getActivePokemon();
        if (attacker == null || attacker.getSpecialCondition() == null) return ctx;

        SpecialCondition condition = attacker.getSpecialCondition();

        if (condition == SpecialCondition.PARALYZED) {
            ctx.getBoard().log(attacker.getPokemon().getName() + " is paralyzed and cannot attack!");
            attacker.setSpecialCondition(null);
            publisher.publish(GameEvent.of(GameEventType.STATUS_REMOVED, ctx.getAttackerBoard().getPlayerId(),
                    Map.of("condition", "PARALYZED", "pokemon", attacker.getPokemon().getName())));
            ctx.setCancelled(true);
            return ctx;
        }

        if (condition == SpecialCondition.ASLEEP) {
            boolean wakeUp = RANDOM.nextBoolean();
            if (wakeUp) {
                attacker.setSpecialCondition(null);
                publisher.publish(GameEvent.of(GameEventType.STATUS_REMOVED, ctx.getAttackerBoard().getPlayerId(),
                        Map.of("condition", "ASLEEP", "pokemon", attacker.getPokemon().getName())));
                ctx.getBoard().log(attacker.getPokemon().getName() + " woke up!");
            } else {
                ctx.getBoard().log(attacker.getPokemon().getName() + " is asleep and cannot attack!");
                ctx.setCancelled(true);
                return ctx;
            }
        }

        return ctx;
    }
}
