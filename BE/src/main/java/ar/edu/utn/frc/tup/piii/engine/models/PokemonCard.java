package ar.edu.utn.frc.tup.piii.engine.models;

import ar.edu.utn.frc.tup.piii.enums.CardType;
import ar.edu.utn.frc.tup.piii.enums.EnergyType;
import ar.edu.utn.frc.tup.piii.enums.PokemonStage;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PokemonCard implements GameCard {

    private String id;
    private String name;
    private int hp;
    private PokemonStage stage;
    private List<EnergyType> types = new ArrayList<>();
    private List<Attack> attacks = new ArrayList<>();
    private String weakness;
    private String resistance;
    private int retreatCost;
    private String imageUrl;

    @Override
    public CardType getCardType() {
        return CardType.POKEMON;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Attack {
        private String name;
        private List<String> cost = new ArrayList<>();
        private int damage;
        private String effect;
    }
}
