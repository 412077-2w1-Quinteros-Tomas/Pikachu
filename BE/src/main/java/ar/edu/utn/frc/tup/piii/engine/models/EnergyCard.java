package ar.edu.utn.frc.tup.piii.engine.models;

import ar.edu.utn.frc.tup.piii.enums.CardType;
import ar.edu.utn.frc.tup.piii.enums.EnergyType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EnergyCard implements GameCard {

    private String id;
    private String name;
    private EnergyType energyType;

    @Override
    public CardType getCardType() {
        return CardType.ENERGY;
    }
}
