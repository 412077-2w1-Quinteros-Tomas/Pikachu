package ar.edu.utn.frc.tup.piii.engine.combat.steps;

import ar.edu.utn.frc.tup.piii.engine.combat.AttackContext;
import ar.edu.utn.frc.tup.piii.engine.events.GameEventPublisher;

public interface AttackStep {

    AttackContext execute(AttackContext ctx, GameEventPublisher publisher);
}
