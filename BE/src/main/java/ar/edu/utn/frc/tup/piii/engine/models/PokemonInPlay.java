package ar.edu.utn.frc.tup.piii.engine.models;

import ar.edu.utn.frc.tup.piii.enums.SpecialCondition;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PokemonInPlay {

    private String instanceId;
    private PokemonCard pokemon;
    private int currentHp;
    private List<EnergyCard> attachedEnergies = new ArrayList<>();
    private SpecialCondition specialCondition;

    public static PokemonInPlay of(PokemonCard card) {
        PokemonInPlay p = new PokemonInPlay();
        p.instanceId = UUID.randomUUID().toString();
        p.pokemon = card;
        p.currentHp = card.getHp();
        return p;
    }

    public boolean isKnockedOut() {
        return currentHp <= 0;
    }

    public int getTotalEnergyCount() {
        return attachedEnergies != null ? attachedEnergies.size() : 0;
    }
}
