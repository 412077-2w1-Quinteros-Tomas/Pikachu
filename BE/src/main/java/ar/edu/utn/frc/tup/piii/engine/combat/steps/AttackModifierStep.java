package ar.edu.utn.frc.tup.piii.engine.combat.steps;

import ar.edu.utn.frc.tup.piii.engine.combat.AttackContext;
import ar.edu.utn.frc.tup.piii.engine.events.GameEventPublisher;
import org.springframework.stereotype.Component;

@Component
public class AttackModifierStep implements AttackStep {

    @Override
    public AttackContext execute(AttackContext ctx, GameEventPublisher publisher) {
        return ctx;
    }
}
