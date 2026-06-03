package ar.edu.utn.frc.tup.piii.engine.combat.steps;

import ar.edu.utn.frc.tup.piii.engine.combat.AttackContext;
import ar.edu.utn.frc.tup.piii.engine.events.GameEventPublisher;
import ar.edu.utn.frc.tup.piii.engine.models.PokemonInPlay;
import org.springframework.stereotype.Component;

@Component
public class EnergyValidationStep implements AttackStep {

    @Override
    public AttackContext execute(AttackContext ctx, GameEventPublisher publisher) {
        PokemonInPlay attacker = ctx.getAttackerBoard().getActivePokemon();
        if (attacker == null) {
            ctx.setBoard(ctx.getBoard());
            ctx.setCancelled(true);
            return ctx;
        }

        int cost = ctx.getAttack().getCost() != null ? ctx.getAttack().getCost().size() : 0;
        int attached = attacker.getTotalEnergyCount();

        if (attached < cost) {
            ctx.getBoard().log("Not enough energy for " + ctx.getAttack().getName()
                    + " (need " + cost + ", have " + attached + ")");
        }
        return ctx;
    }
}
