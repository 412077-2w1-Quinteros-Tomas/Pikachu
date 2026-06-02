package ar.edu.utn.frc.tup.piii.services;

import ar.edu.utn.frc.tup.piii.dtos.deck.CreateDeckDTO;
import ar.edu.utn.frc.tup.piii.dtos.deck.DeckDTO;
import ar.edu.utn.frc.tup.piii.dtos.deck.DeckValidationResultDTO;
import ar.edu.utn.frc.tup.piii.entities.CardEntity;
import ar.edu.utn.frc.tup.piii.entities.DeckCardEntity;
import ar.edu.utn.frc.tup.piii.entities.DeckEntity;
import ar.edu.utn.frc.tup.piii.enums.CardType;
import ar.edu.utn.frc.tup.piii.enums.PokemonStage;
import ar.edu.utn.frc.tup.piii.exceptions.EntityNotFoundException;
import ar.edu.utn.frc.tup.piii.mappers.DeckMapper;
import ar.edu.utn.frc.tup.piii.repositories.CardRepository;
import ar.edu.utn.frc.tup.piii.repositories.DeckRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DeckServiceTest {

    @Mock
    private DeckRepository deckRepository;

    @Mock
    private CardRepository cardRepository;

    @Mock
    private DeckMapper deckMapper;

    @Mock
    private DeckValidationService validationService;

    @InjectMocks
    private DeckService deckService;

    private CardEntity basicPokemon;
    private CardEntity energyCard;

    @BeforeEach
    void setUp() {
        basicPokemon = new CardEntity();
        basicPokemon.setName("Bulbasaur");
        basicPokemon.setCardType(CardType.POKEMON);
        basicPokemon.setStage(PokemonStage.BASIC);
        basicPokemon.setSetId("xy1");

        energyCard = new CardEntity();
        energyCard.setName("Grass Energy");
        energyCard.setCardType(CardType.ENERGY);
        energyCard.setSetId("xy1");
    }

    @Test
    void getDeckById_notFound_throwsException() {
        UUID id = UUID.randomUUID();
        when(deckRepository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> deckService.getDeckById(id))
                .isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    void getDeckById_found_returnsMapped() {
        UUID id = UUID.randomUUID();
        DeckEntity entity = new DeckEntity();
        DeckDTO dto = new DeckDTO();
        when(deckRepository.findById(id)).thenReturn(Optional.of(entity));
        when(deckMapper.toDto(entity)).thenReturn(dto);

        DeckDTO result = deckService.getDeckById(id);

        assertThat(result).isSameAs(dto);
    }

    @Test
    void createDeck_savesAndReturnsMapped() {
        UUID cardId = UUID.randomUUID();
        CreateDeckDTO dto = new CreateDeckDTO("My Deck", "desc",
                List.of(new CreateDeckDTO.CardEntryDTO(cardId, 4)));

        DeckEntity saved = new DeckEntity();
        saved.setName("My Deck");
        DeckDTO expected = new DeckDTO();

        when(cardRepository.findById(cardId)).thenReturn(Optional.of(basicPokemon));
        when(deckRepository.save(any(DeckEntity.class))).thenReturn(saved);
        when(deckMapper.toDto(saved)).thenReturn(expected);

        DeckDTO result = deckService.createDeck(dto);

        assertThat(result).isSameAs(expected);
        verify(deckRepository).save(any(DeckEntity.class));
    }

    @Test
    void deleteDeck_notFound_throwsException() {
        UUID id = UUID.randomUUID();
        when(deckRepository.existsById(id)).thenReturn(false);

        assertThatThrownBy(() -> deckService.deleteDeck(id))
                .isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    void deleteDeck_found_deletesById() {
        UUID id = UUID.randomUUID();
        when(deckRepository.existsById(id)).thenReturn(true);

        deckService.deleteDeck(id);

        verify(deckRepository).deleteById(id);
    }

    @Test
    void validateDeck_delegatesToValidationService() {
        UUID id = UUID.randomUUID();
        DeckEntity deck = new DeckEntity();
        DeckValidationResultDTO result = new DeckValidationResultDTO(true, List.of(), 60);

        when(deckRepository.findById(id)).thenReturn(Optional.of(deck));
        when(validationService.validate(deck)).thenReturn(result);

        DeckValidationResultDTO actual = deckService.validateDeck(id);

        assertThat(actual).isSameAs(result);
    }
}
