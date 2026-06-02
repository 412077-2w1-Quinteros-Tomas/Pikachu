package ar.edu.utn.frc.tup.piii.entities;

import ar.edu.utn.frc.tup.piii.enums.CardType;
import ar.edu.utn.frc.tup.piii.enums.PokemonStage;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "cards")
@Getter
@Setter
@NoArgsConstructor
public class CardEntity extends BaseEntity {

    @Column(nullable = false, unique = true)
    private String externalId;

    @Column(nullable = false)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CardType cardType;

    private Integer hp;

    @Enumerated(EnumType.STRING)
    private PokemonStage stage;

    /** JSON array of EnergyType strings, e.g. ["FIRE"]. */
    @Column(columnDefinition = "TEXT")
    private String types;

    /** JSON array of attack objects: [{name, cost, damage, effect}]. */
    @Column(columnDefinition = "TEXT")
    private String attacks;

    /** JSON array of ability objects: [{name, effect}]. */
    @Column(columnDefinition = "TEXT")
    private String abilities;

    private String weakness;

    private String resistance;

    private Integer retreatCost;

    @Column(length = 512)
    private String imageUrl;

    private String rarity;

    private String cardNumber;

    @Column(nullable = false)
    private String setId = "xy1";
}
