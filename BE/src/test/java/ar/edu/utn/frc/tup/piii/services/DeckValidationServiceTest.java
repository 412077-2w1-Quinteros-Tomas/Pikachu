package ar.edu.utn.frc.tup.piii.services;

import ar.edu.utn.frc.tup.piii.dtos.deck.DeckValidationResultDTO;
import ar.edu.utn.frc.tup.piii.entities.CardEntity;
import ar.edu.utn.frc.tup.piii.entities.DeckCardEntity;
import ar.edu.utn.frc.tup.piii.entities.DeckEntity;
import ar.edu.utn.frc.tup.piii.enums.CardType;
import ar.edu.utn.frc.tup.piii.enums.PokemonStage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class DeckValidationServiceTest {

    private DeckValidationService service;

    @BeforeEach
    void setUp() {
        service = new DeckValidationService();
    }

    @Test
    void validDeck_passes() {
        DeckEntity deck = deckWith60Cards();
        DeckValidationResultDTO result = service.validate(deck);
        assertThat(result.isValid()).isTrue();
        assertThat(result.getErrors()).isEmpty();
        assertThat(result.getTotalCards()).isEqualTo(60);
    }

    @Test
    void wrongSize_fails() {
        DeckEntity deck = new DeckEntity();
        deck.setCards(new ArrayList<>(List.of(entry(pokemon("Bulbasaur"), 10))));

        DeckValidationResultDTO result = service.validate(deck);
        assertThat(result.isValid()).isFalse();
        assertThat(result.getErrors()).anyMatch(e -> e.contains("exactly 60"));
    }

    @Test
    void tooManyCopies_fails() {
        DeckEntity deck = new DeckEntity();
        List<DeckCardEntity> cards = new ArrayList<>();
        cards.add(entry(pokemon("Bulbasaur"), 5));
        cards.add(fillerCards(55));
        deck.setCards(cards);

        DeckValidationResultDTO result = service.validate(deck);
        assertThat(result.isValid()).isFalse();
        assertThat(result.getErrors()).anyMatch(e -> e.contains("Bulbasaur"));
    }

    @Test
    void noBasicPokemon_fails() {
        DeckEntity deck = new DeckEntity();
        List<DeckCardEntity> cards = new ArrayList<>();
        cards.add(entry(trainerCard("Bill"), 4));
        cards.add(entry(energyCard("Fire Energy"), 56));
        deck.setCards(cards);

        DeckValidationResultDTO result = service.validate(deck);
        assertThat(result.isValid()).isFalse();
        assertThat(result.getErrors()).anyMatch(e -> e.contains("Basic Pokémon"));
    }

    @Test
    void basicEnergyExemptFromCopyLimit() {
        DeckEntity deck = new DeckEntity();
        List<DeckCardEntity> cards = new ArrayList<>();
        cards.add(entry(pokemon("Bulbasaur"), 4));
        cards.add(entry(energyCard("Grass Energy"), 56));
        deck.setCards(cards);

        DeckValidationResultDTO result = service.validate(deck);
        assertThat(result.getErrors()).noneMatch(e -> e.contains("Grass Energy"));
    }

    private DeckEntity deckWith60Cards() {
        DeckEntity deck = new DeckEntity();
        List<DeckCardEntity> cards = new ArrayList<>();
        cards.add(entry(pokemon("Bulbasaur"), 4));
        cards.add(entry(pokemon("Ivysaur"), 4));
        cards.add(entry(trainerCard("Bill"), 4));
        cards.add(entry(energyCard("Grass Energy"), 48));
        deck.setCards(cards);
        return deck;
    }

    private DeckCardEntity fillerCards(int qty) {
        return entry(energyCard("Fire Energy"), qty);
    }

    private DeckCardEntity entry(CardEntity card, int quantity) {
        DeckCardEntity dc = new DeckCardEntity();
        dc.setCard(card);
        dc.setQuantity(quantity);
        return dc;
    }

    private CardEntity pokemon(String name) {
        CardEntity c = new CardEntity();
        c.setName(name);
        c.setCardType(CardType.POKEMON);
        c.setStage(PokemonStage.BASIC);
        c.setSetId("xy1");
        return c;
    }

    private CardEntity trainerCard(String name) {
        CardEntity c = new CardEntity();
        c.setName(name);
        c.setCardType(CardType.TRAINER);
        c.setSetId("xy1");
        return c;
    }

    private CardEntity energyCard(String name) {
        CardEntity c = new CardEntity();
        c.setName(name);
        c.setCardType(CardType.ENERGY);
        c.setSetId("xy1");
        return c;
    }
}
