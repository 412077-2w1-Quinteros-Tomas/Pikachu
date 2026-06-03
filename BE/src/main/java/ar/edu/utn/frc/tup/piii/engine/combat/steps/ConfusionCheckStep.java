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
public class ConfusionCheckStep implements AttackStep {

    private static final int CONFUSION_SELF_DAMAGE = 30;
    private static final Random RANDOM = new Random();

    @Override
    public AttackContext execute(AttackContext ctx, GameEventPublisher publisher) {
        PokemonInPlay attacker = ctx.getAttackerBoard().getActivePokemon();
        if (attacker == null || attacker.getSpecialCondition() != SpecialCondition.CONFUSED) return ctx;

        boolean headsAttack = RANDOM.nextBoolean();
        if (!headsAttack) {
            int newHp = Math.max(0, attacker.getCurrentHp() - CONFUSION_SELF_DAMAGE);
            attacker.setCurrentHp(newHp);
            ctx.getBoard().log(attacker.getPokemon().getName() + " hurt itself in confusion for "
                    + CONFUSION_SELF_DAMAGE + " damage!");
            publisher.publish(GameEvent.of(GameEventType.DAMAGE_DEALT, ctx.getAttackerBoard().getPlayerId(),
                    Map.of("damage", CONFUSION_SELF_DAMAGE, "source", "CONFUSION",
                            "pokemon", attacker.getPokemon().getName())));
            ctx.setSelfDamage(CONFUSION_SELF_DAMAGE);
            ctx.setCancelled(true);
        }
        return ctx;
    }
}
