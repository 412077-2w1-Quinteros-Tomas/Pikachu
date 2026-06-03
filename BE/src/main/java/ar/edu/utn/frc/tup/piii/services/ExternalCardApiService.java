package ar.edu.utn.frc.tup.piii.services;

import ar.edu.utn.frc.tup.piii.dtos.card.CardDTO;
import ar.edu.utn.frc.tup.piii.enums.CardType;
import ar.edu.utn.frc.tup.piii.enums.EnergyType;
import ar.edu.utn.frc.tup.piii.enums.PokemonStage;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.annotation.PostConstruct;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ExternalCardApiService {

    private static final int PAGE_SIZE = 250;

    @Value("${pokemontcg.api.base-url:https://api.pokemontcg.io/v2}")
    private String baseUrl;

    @Value("${pokemontcg.api.key:}")
    private String apiKey;

    private RestClient restClient;

    @PostConstruct
    void init() {
        RestClient.Builder builder = RestClient.builder().baseUrl(baseUrl);
        if (apiKey != null && !apiKey.isBlank()) {
            builder.defaultHeader("X-Api-Key", apiKey);
        }
        restClient = builder.build();
    }

    public List<CardDTO> fetchSetCards(String setId) {
        List<CardDTO> result = new ArrayList<>();
        int page = 1;

        while (true) {
            ApiResponse response = restClient.get()
                    .uri("/cards?q=set.id:{setId}&pageSize={size}&page={page}",
                            setId, PAGE_SIZE, page)
                    .retrieve()
                    .body(ApiResponse.class);

            if (response == null || response.getData() == null || response.getData().isEmpty()) {
                break;
            }

            response.getData().stream()
                    .map(this::toCardDto)
                    .forEach(result::add);

            if (result.size() >= response.getTotalCount()) {
                break;
            }
            page++;
        }

        return result;
    }

    private CardDTO toCardDto(ApiCard card) {
        CardDTO dto = new CardDTO();
        dto.setExternalId(card.getId());
        dto.setName(card.getName());
        dto.setCardType(mapSupertype(card.getSupertype()));
        dto.setHp(parseHp(card.getHp()));
        dto.setStage(mapStage(card.getSubtypes()));
        dto.setTypes(mapTypes(card.getTypes()));
        dto.setWeakness(extractFirstType(card.getWeaknesses()));
        dto.setResistance(extractFirstType(card.getResistances()));
        dto.setRetreatCost(card.getRetreatCost() != null ? card.getRetreatCost().size() : 0);
        dto.setImageUrl(card.getImages() != null ? card.getImages().getSmall() : null);
        dto.setRarity(card.getRarity());
        dto.setCardNumber(card.getNumber());
        dto.setSetId(card.getSet() != null ? card.getSet().getId() : "xy1");
        dto.setAttacks(mapAttacks(card.getAttacks()));
        dto.setAbilities(mapAbilities(card.getAbilities()));
        return dto;
    }

    private CardType mapSupertype(String supertype) {
        if (supertype == null) {
            return CardType.POKEMON;
        }
        return switch (supertype) {
            case "Trainer" -> CardType.TRAINER;
            case "Energy"  -> CardType.ENERGY;
            default        -> CardType.POKEMON;
        };
    }

    private PokemonStage mapStage(List<String> subtypes) {
        if (subtypes == null) {
            return null;
        }
        for (String sub : subtypes) {
            PokemonStage stage = switch (sub) {
                case "Basic"   -> PokemonStage.BASIC;
                case "Stage 1" -> PokemonStage.STAGE1;
                case "Stage 2" -> PokemonStage.STAGE2;
                case "EX"      -> PokemonStage.EX;
                case "MEGA"    -> PokemonStage.MEGA;
                default        -> null;
            };
            if (stage != null) {
                return stage;
            }
        }
        return null;
    }

    private List<EnergyType> mapTypes(List<String> types) {
        if (types == null) {
            return List.of();
        }
        return types.stream().map(EnergyType::fromApiName).toList();
    }

    private String extractFirstType(List<ApiTypeValue> list) {
        if (list == null || list.isEmpty()) {
            return null;
        }
        return list.get(0).getType();
    }

    private List<CardDTO.AttackDTO> mapAttacks(List<ApiAttack> attacks) {
        if (attacks == null) {
            return List.of();
        }
        return attacks.stream().map(a -> new CardDTO.AttackDTO(
                a.getName(),
                a.getCost(),
                a.getDamage(),
                a.getText()
        )).toList();
    }

    private List<CardDTO.AbilityDTO> mapAbilities(List<ApiAbility> abilities) {
        if (abilities == null) {
            return List.of();
        }
        return abilities.stream().map(a -> new CardDTO.AbilityDTO(
                a.getName(),
                a.getText()
        )).toList();
    }

    private Integer parseHp(String hp) {
        if (hp == null || hp.isBlank()) {
            return null;
        }
        try {
            return Integer.parseInt(hp);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    // ── API response models ──────────────────────────────────────────────────

    @Data
    @NoArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    static class ApiResponse {
        private List<ApiCard> data;
        private int totalCount;
    }

    @Data
    @NoArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    static class ApiCard {
        private String id;
        private String name;
        private String supertype;
        private List<String> subtypes;
        private String hp;
        private List<String> types;
        private List<ApiAttack> attacks;
        private List<ApiAbility> abilities;
        private List<ApiTypeValue> weaknesses;
        private List<ApiTypeValue> resistances;
        private List<String> retreatCost;
        private String number;
        private String rarity;
        private ApiImages images;
        private ApiSet set;
    }

    @Data
    @NoArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    static class ApiAttack {
        private String name;
        private List<String> cost;
        private String damage;
        @JsonProperty("text")
        private String text;
    }

    @Data
    @NoArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    static class ApiAbility {
        private String name;
        @JsonProperty("text")
        private String text;
    }

    @Data
    @NoArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    static class ApiTypeValue {
        private String type;
        private String value;
    }

    @Data
    @NoArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    static class ApiImages {
        private String small;
        private String large;
    }

    @Data
    @NoArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    static class ApiSet {
        private String id;
        private String name;
    }
}
