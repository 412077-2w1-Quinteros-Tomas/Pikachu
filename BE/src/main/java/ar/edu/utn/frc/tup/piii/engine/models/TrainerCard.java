package ar.edu.utn.frc.tup.piii.engine.models;

import ar.edu.utn.frc.tup.piii.enums.CardType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TrainerCard implements GameCard {

    private String id;
    private String name;
    private String effect;

    @Override
    public CardType getCardType() {
        return CardType.TRAINER;
    }
}
