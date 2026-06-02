package ar.edu.utn.frc.tup.piii.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum EnergyType {

    FIRE("Fire"),
    WATER("Water"),
    GRASS("Grass"),
    LIGHTNING("Lightning"),
    PSYCHIC("Psychic"),
    FIGHTING("Fighting"),
    DARKNESS("Darkness"),
    METAL("Metal"),
    FAIRY("Fairy"),
    DRAGON("Dragon"),
    COLORLESS("Colorless");

    /** Value used by the pokemontcg.io API for this energy type. */
    private final String apiName;

    public static EnergyType fromApiName(String apiName) {
        for (EnergyType type : values()) {
            if (type.apiName.equalsIgnoreCase(apiName)) {
                return type;
            }
        }
        return COLORLESS;
    }
}
