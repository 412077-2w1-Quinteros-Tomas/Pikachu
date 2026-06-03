package ar.edu.utn.frc.tup.piii.engine.combat;

import ar.edu.utn.frc.tup.piii.engine.models.GameBoard;
import ar.edu.utn.frc.tup.piii.engine.models.PlayerBoard;
import ar.edu.utn.frc.tup.piii.engine.models.PokemonCard;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AttackContext {

    private GameBoard board;
    private PlayerBoard attackerBoard;
    private PlayerBoard defenderBoard;
    private PokemonCard.Attack attack;
    private int baseDamage;
    private int finalDamage;
    private boolean cancelled;
    private int selfDamage;
}
