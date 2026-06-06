package ar.edu.utn.frc.tup.piii.engine.combat.steps;

import ar.edu.utn.frc.tup.piii.engine.combat.AttackContext;
import ar.edu.utn.frc.tup.piii.engine.events.GameEventPublisher;
import ar.edu.utn.frc.tup.piii.engine.models.EnergyCard;
import ar.edu.utn.frc.tup.piii.engine.models.PokemonInPlay;
import ar.edu.utn.frc.tup.piii.enums.EnergyType;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class EnergyValidationStep implements AttackStep {

    @Override
    public AttackContext execute(AttackContext ctx, GameEventPublisher publisher) {
        PokemonInPlay attacker = ctx.getAttackerBoard().getActivePokemon();
        if (attacker == null) {
            ctx.setCancelled(true);
            return ctx;
        }

        List<String> required = ctx.getAttack().getCost() != null
                ? ctx.getAttack().getCost() : List.of();
        if (required.isEmpty()) return ctx;

        // Build mutable list of available energy types
        List<EnergyType> available = attacker.getAttachedEnergies() != null
                ? attacker.getAttachedEnergies().stream()
                        .map(EnergyCard::getEnergyType)
                        .collect(Collectors.toCollection(ArrayList::new))
                : new ArrayList<>();

        // First pass: satisfy specific (non-Colorless) requirements
        List<String> colorlessSlots = new ArrayList<>();
        for (String req : required) {
            EnergyType reqType = EnergyType.fromApiName(req);
            if (reqType == EnergyType.COLORLESS) {
                colorlessSlots.add(req);
            } else {
                int idx = available.indexOf(reqType);
                if (idx >= 0) {
                    available.remove(idx);
                } else {
                    cancel(ctx, required, attacker.getTotalEnergyCount());
                    return ctx;
                }
            }
        }

        // Second pass: Colorless can be satisfied by any remaining energy
        if (available.size() < colorlessSlots.size()) {
            cancel(ctx, required, attacker.getTotalEnergyCount());
            return ctx;
        }

        return ctx;
    }

    private void cancel(AttackContext ctx, List<String> required, int have) {
        ctx.setCancelled(true);
        ctx.getBoard().log("Not enough energy for " + ctx.getAttack().getName()
                + " (need " + required + ", have " + have + ")");
    }
}
