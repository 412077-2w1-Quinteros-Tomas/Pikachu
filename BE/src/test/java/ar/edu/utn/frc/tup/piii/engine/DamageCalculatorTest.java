package ar.edu.utn.frc.tup.piii.engine;

import ar.edu.utn.frc.tup.piii.engine.combat.DamageCalculator;
import ar.edu.utn.frc.tup.piii.enums.EnergyType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class DamageCalculatorTest {

    private DamageCalculator calculator;

    @BeforeEach
    void setUp() {
        calculator = new DamageCalculator();
    }

    @Test
    void applyWeakness_doublesWhenTypeMatches() {
        int result = calculator.applyWeakness(30, List.of(EnergyType.FIRE), "FIRE");
        assertThat(result).isEqualTo(60);
    }

    @Test
    void applyWeakness_noChangeWhenTypeDoesNotMatch() {
        int result = calculator.applyWeakness(30, List.of(EnergyType.WATER), "FIRE");
        assertThat(result).isEqualTo(30);
    }

    @Test
    void applyWeakness_noChangeWhenWeaknessIsNull() {
        int result = calculator.applyWeakness(30, List.of(EnergyType.FIRE), null);
        assertThat(result).isEqualTo(30);
    }

    @Test
    void applyResistance_reduces30WhenTypeMatches() {
        int result = calculator.applyResistance(50, List.of(EnergyType.FIRE), "FIRE");
        assertThat(result).isEqualTo(20);
    }

    @Test
    void applyResistance_noChangeWhenTypeDoesNotMatch() {
        int result = calculator.applyResistance(50, List.of(EnergyType.WATER), "FIRE");
        assertThat(result).isEqualTo(50);
    }

    @Test
    void applyResistance_neverBelowZero() {
        int result = calculator.applyResistance(10, List.of(EnergyType.FIRE), "FIRE");
        assertThat(result).isEqualTo(0);
    }

    @Test
    void calculateFinalDamage_appliesWeaknessThenResistance() {
        int result = calculator.calculateFinalDamage(30,
                List.of(EnergyType.FIRE), "FIRE", null);
        assertThat(result).isEqualTo(60);
    }

    @Test
    void calculateFinalDamage_zeroDamageRemainsZero() {
        int result = calculator.calculateFinalDamage(0,
                List.of(EnergyType.FIRE), null, null);
        assertThat(result).isEqualTo(0);
    }

    @Test
    void applyWeakness_noChangeWhenAttackerTypesEmpty() {
        int result = calculator.applyWeakness(30, List.of(), "FIRE");
        assertThat(result).isEqualTo(30);
    }
}
