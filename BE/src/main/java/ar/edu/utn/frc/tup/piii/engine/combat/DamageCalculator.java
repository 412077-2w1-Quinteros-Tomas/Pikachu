package ar.edu.utn.frc.tup.piii.engine.combat;

import ar.edu.utn.frc.tup.piii.enums.EnergyType;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class DamageCalculator {

    private static final int RESISTANCE_REDUCTION = 30;

    public int applyWeakness(int damage, List<EnergyType> attackerTypes, String weakness) {
        if (weakness == null || weakness.isBlank() || attackerTypes == null) return damage;
        for (EnergyType type : attackerTypes) {
            if (weakness.toUpperCase().contains(type.name())) {
                return damage * 2;
            }
        }
        return damage;
    }

    public int applyResistance(int damage, List<EnergyType> attackerTypes, String resistance) {
        if (resistance == null || resistance.isBlank() || attackerTypes == null) return damage;
        for (EnergyType type : attackerTypes) {
            if (resistance.toUpperCase().contains(type.name())) {
                return Math.max(0, damage - RESISTANCE_REDUCTION);
            }
        }
        return damage;
    }

    public int calculateFinalDamage(int base, List<EnergyType> attackerTypes,
                                    String weakness, String resistance) {
        int damage = applyWeakness(base, attackerTypes, weakness);
        damage = applyResistance(damage, attackerTypes, resistance);
        return Math.max(0, damage);
    }
}
