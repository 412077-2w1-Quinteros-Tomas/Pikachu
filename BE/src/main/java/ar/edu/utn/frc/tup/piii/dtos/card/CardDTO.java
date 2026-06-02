package ar.edu.utn.frc.tup.piii.dtos.card;

import ar.edu.utn.frc.tup.piii.enums.CardType;
import ar.edu.utn.frc.tup.piii.enums.EnergyType;
import ar.edu.utn.frc.tup.piii.enums.PokemonStage;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CardDTO {

    private String id;
    private String externalId;
    private String name;
    private CardType cardType;
    private Integer hp;
    private PokemonStage stage;
    private List<EnergyType> types;
    private List<AttackDTO> attacks;
    private List<AbilityDTO> abilities;
    private String weakness;
    private String resistance;
    private Integer retreatCost;
    private String imageUrl;
    private String rarity;
    private String cardNumber;
    private String setId;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AttackDTO {
        private String name;
        private List<String> cost;
        private String damage;
        private String effect;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AbilityDTO {
        private String name;
        private String effect;
    }
}
