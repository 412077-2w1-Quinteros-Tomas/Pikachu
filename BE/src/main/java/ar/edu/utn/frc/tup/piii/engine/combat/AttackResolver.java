package ar.edu.utn.frc.tup.piii.engine.combat;

import ar.edu.utn.frc.tup.piii.engine.combat.steps.AttackModifierStep;
import ar.edu.utn.frc.tup.piii.engine.combat.steps.ConfusionCheckStep;
import ar.edu.utn.frc.tup.piii.engine.combat.steps.DamageCalculationStep;
import ar.edu.utn.frc.tup.piii.engine.combat.steps.EnergyValidationStep;
import ar.edu.utn.frc.tup.piii.engine.combat.steps.PostDamageEffectStep;
import ar.edu.utn.frc.tup.piii.engine.combat.steps.PreAttackEffectStep;
import ar.edu.utn.frc.tup.piii.engine.combat.steps.TargetSelectionStep;
import ar.edu.utn.frc.tup.piii.engine.events.GameEvent;
import ar.edu.utn.frc.tup.piii.engine.events.GameEventPublisher;
import ar.edu.utn.frc.tup.piii.engine.events.GameEventType;
import ar.edu.utn.frc.tup.piii.engine.models.GameBoard;
import ar.edu.utn.frc.tup.piii.engine.models.PlayerBoard;
import ar.edu.utn.frc.tup.piii.engine.models.PokemonCard;
import ar.edu.utn.frc.tup.piii.engine.models.PokemonInPlay;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@RequiredArgsConstructor
public class AttackResolver {

    private final EnergyValidationStep energyValidationStep;
    private final PreAttackEffectStep preAttackEffectStep;
    private final ConfusionCheckStep confusionCheckStep;
    private final TargetSelectionStep targetSelectionStep;
    private final DamageCalculationStep damageCalculationStep;
    private final AttackModifierStep attackModifierStep;
    private final PostDamageEffectStep postDamageEffectStep;

    public AttackContext resolve(GameBoard board, String attackerId,
                                 int attackIndex, GameEventPublisher publisher) {
        PlayerBoard attackerBoard = board.getBoardFor(attackerId);
        PlayerBoard defenderBoard = board.getOpponentBoard(attackerId);

        if (attackerBoard == null || defenderBoard == null) {
            return cancelledContext(board, attackerBoard, defenderBoard);
        }

        PokemonInPlay attackerInPlay = attackerBoard.getActivePokemon();
        if (attackerInPlay == null) return cancelledContext(board, attackerBoard, defenderBoard);

        PokemonCard attackerCard = attackerInPlay.getPokemon();
        if (attackerCard.getAttacks() == null || attackerCard.getAttacks().isEmpty()) {
            return cancelledContext(board, attackerBoard, defenderBoard);
        }

        int safeIndex = Math.min(attackIndex, attackerCard.getAttacks().size() - 1);
        PokemonCard.Attack attack = attackerCard.getAttacks().get(safeIndex);

        AttackContext ctx = AttackContext.builder()
                .board(board)
                .attackerBoard(attackerBoard)
                .defenderBoard(defenderBoard)
                .attack(attack)
                .baseDamage(attack.getDamage())
                .finalDamage(attack.getDamage())
                .cancelled(false)
                .selfDamage(0)
                .build();

        ctx = energyValidationStep.execute(ctx, publisher);
        if (ctx.isCancelled()) return ctx;

        ctx = preAttackEffectStep.execute(ctx, publisher);
        if (ctx.isCancelled()) return ctx;

        ctx = confusionCheckStep.execute(ctx, publisher);
        if (ctx.isCancelled()) return ctx;

        ctx = targetSelectionStep.execute(ctx, publisher);
        if (ctx.isCancelled()) return ctx;

        ctx = damageCalculationStep.execute(ctx, publisher);
        ctx = attackModifierStep.execute(ctx, publisher);

        applyDamage(ctx, publisher);

        ctx = postDamageEffectStep.execute(ctx, publisher);

        return ctx;
    }

    private void applyDamage(AttackContext ctx, GameEventPublisher publisher) {
        PokemonInPlay defender = ctx.getDefenderBoard().getActivePokemon();
        if (defender == null) return;

        int damage = ctx.getFinalDamage();
        if (damage <= 0) return;

        defender.setCurrentHp(Math.max(0, defender.getCurrentHp() - damage));

        publisher.publish(GameEvent.of(GameEventType.DAMAGE_DEALT, ctx.getAttackerBoard().getPlayerId(),
                Map.of("damage", damage, "target", defender.getPokemon().getName(),
                        "attack", ctx.getAttack().getName())));

        ctx.getBoard().log(ctx.getAttack().getName() + " dealt " + damage + " damage to "
                + defender.getPokemon().getName());
    }

    private AttackContext cancelledContext(GameBoard board, PlayerBoard attackerBoard, PlayerBoard defenderBoard) {
        return AttackContext.builder()
                .board(board)
                .attackerBoard(attackerBoard)
                .defenderBoard(defenderBoard)
                .cancelled(true)
                .build();
    }
}
