package ar.edu.utn.frc.tup.piii.dtos.card;

import ar.edu.utn.frc.tup.piii.enums.CardType;
import ar.edu.utn.frc.tup.piii.enums.EnergyType;
import ar.edu.utn.frc.tup.piii.enums.PokemonStage;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class CardFilterDTO {

    private String name;
    private CardType cardType;
    private EnergyType type;
    private PokemonStage stage;
    private String setId;
    private Integer page = 0;
    private Integer size = 20;
}
