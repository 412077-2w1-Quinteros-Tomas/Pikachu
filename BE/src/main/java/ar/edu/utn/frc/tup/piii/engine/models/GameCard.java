package ar.edu.utn.frc.tup.piii.engine.models;

import ar.edu.utn.frc.tup.piii.enums.CardType;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "cardType")
@JsonSubTypes({
    @JsonSubTypes.Type(value = PokemonCard.class, name = "POKEMON"),
    @JsonSubTypes.Type(value = EnergyCard.class, name = "ENERGY"),
    @JsonSubTypes.Type(value = TrainerCard.class, name = "TRAINER")
})
public interface GameCard {
    String getId();
    String getName();
    CardType getCardType();
}
