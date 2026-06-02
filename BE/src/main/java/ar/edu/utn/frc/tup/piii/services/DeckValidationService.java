package ar.edu.utn.frc.tup.piii.services;

import ar.edu.utn.frc.tup.piii.dtos.deck.DeckValidationResultDTO;
import ar.edu.utn.frc.tup.piii.entities.DeckCardEntity;
import ar.edu.utn.frc.tup.piii.entities.DeckEntity;
import ar.edu.utn.frc.tup.piii.enums.CardType;
import ar.edu.utn.frc.tup.piii.enums.PokemonStage;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class DeckValidationService {

    private static final int REQUIRED_DECK_SIZE = 60;
    private static final int MAX_COPIES_PER_NAME = 4;

    public DeckValidationResultDTO validate(DeckEntity deck) {
        List<String> errors = new ArrayList<>();
        List<DeckCardEntity> cards = deck.getCards();

        int total = cards.stream().mapToInt(DeckCardEntity::getQuantity).sum();

        if (total != REQUIRED_DECK_SIZE) {
            errors.add("Deck must have exactly " + REQUIRED_DECK_SIZE + " cards (currently " + total + ")");
        }

        Map<String, Integer> countsByName = cards.stream()
                .filter(dc -> !isBasicEnergy(dc))
                .collect(Collectors.toMap(
                        dc -> dc.getCard().getName(),
                        DeckCardEntity::getQuantity,
                        Integer::sum));

        countsByName.entrySet().stream()
                .filter(e -> e.getValue() > MAX_COPIES_PER_NAME)
                .forEach(e -> errors.add(
                        "Card \"" + e.getKey() + "\" exceeds the limit of " + MAX_COPIES_PER_NAME + " copies (" + e.getValue() + " found)"));

        boolean hasBasicPokemon = cards.stream().anyMatch(dc ->
                dc.getCard().getCardType() == CardType.POKEMON
                        && dc.getCard().getStage() == PokemonStage.BASIC
                        && dc.getQuantity() > 0);

        if (!hasBasicPokemon) {
            errors.add("Deck must contain at least 1 Basic Pokémon");
        }

        return new DeckValidationResultDTO(errors.isEmpty(), errors, total);
    }

    private boolean isBasicEnergy(DeckCardEntity dc) {
        return dc.getCard().getCardType() == CardType.ENERGY
                && dc.getCard().getStage() == null;
    }
}
