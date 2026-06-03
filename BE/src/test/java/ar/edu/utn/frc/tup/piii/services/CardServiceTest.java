package ar.edu.utn.frc.tup.piii.services;

import ar.edu.utn.frc.tup.piii.dtos.card.CardDTO;
import ar.edu.utn.frc.tup.piii.dtos.card.CardFilterDTO;
import ar.edu.utn.frc.tup.piii.entities.CardEntity;
import ar.edu.utn.frc.tup.piii.enums.CardType;
import ar.edu.utn.frc.tup.piii.enums.PokemonStage;
import ar.edu.utn.frc.tup.piii.exceptions.EntityNotFoundException;
import ar.edu.utn.frc.tup.piii.mappers.CardMapper;
import ar.edu.utn.frc.tup.piii.repositories.CardRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CardServiceTest {

    @Mock private CardRepository cardRepository;
    @Mock private CardMapper cardMapper;
    @Mock private ExternalCardApiService externalCardApiService;

    @InjectMocks private CardService cardService;

    private CardEntity pokemonEntity;
    private CardDTO pokemonDto;

    @BeforeEach
    void setUp() {
        pokemonEntity = new CardEntity();
        pokemonEntity.setName("Bulbasaur");
        pokemonEntity.setCardType(CardType.POKEMON);
        pokemonEntity.setStage(PokemonStage.BASIC);
        pokemonEntity.setSetId("xy1");

        pokemonDto = new CardDTO();
        pokemonDto.setName("Bulbasaur");
        pokemonDto.setCardType(CardType.POKEMON);
        pokemonDto.setSetId("xy1");
    }

    @Test
    void getById_found_returnsDto() {
        UUID id = UUID.randomUUID();
        when(cardRepository.findById(id)).thenReturn(Optional.of(pokemonEntity));
        when(cardMapper.toDto(pokemonEntity)).thenReturn(pokemonDto);

        CardDTO result = cardService.getById(id);

        assertThat(result).isSameAs(pokemonDto);
    }

    @Test
    void getById_notFound_throwsEntityNotFoundException() {
        UUID id = UUID.randomUUID();
        when(cardRepository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> cardService.getById(id))
                .isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    void getCards_returnsFilteredList() {
        CardFilterDTO filter = new CardFilterDTO();
        filter.setSetId("xy1");
        filter.setPage(0);
        filter.setSize(10);

        when(cardRepository.findBySetId("xy1")).thenReturn(List.of(pokemonEntity));
        when(cardMapper.toDto(pokemonEntity)).thenReturn(pokemonDto);

        List<CardDTO> result = cardService.getCards(filter);

        assertThat(result).hasSize(1);
        assertThat(result.get(0)).isSameAs(pokemonDto);
    }

    @Test
    void getCards_withNameFilter_filtersCorrectly() {
        CardFilterDTO filter = new CardFilterDTO();
        filter.setSetId("xy1");
        filter.setName("bulba");
        filter.setPage(0);
        filter.setSize(10);

        CardEntity other = new CardEntity();
        other.setName("Charmander");
        other.setCardType(CardType.POKEMON);
        other.setSetId("xy1");

        when(cardRepository.findBySetId("xy1")).thenReturn(List.of(pokemonEntity, other));
        when(cardMapper.toDto(pokemonEntity)).thenReturn(pokemonDto);

        List<CardDTO> result = cardService.getCards(filter);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName()).isEqualTo("Bulbasaur");
    }

    @Test
    void searchByName_delegatesToRepository() {
        when(cardRepository.findByNameContainingIgnoreCase("bulba")).thenReturn(List.of(pokemonEntity));
        when(cardMapper.toDtoList(any())).thenReturn(List.of(pokemonDto));

        List<CardDTO> result = cardService.searchByName("bulba");

        assertThat(result).hasSize(1);
        verify(cardRepository).findByNameContainingIgnoreCase("bulba");
    }

    @Test
    void syncSet_fetchesAndSaves() {
        when(externalCardApiService.fetchSetCards("xy1")).thenReturn(List.of(pokemonDto));
        when(cardRepository.findByExternalId(any())).thenReturn(Optional.empty());
        when(cardMapper.toEntity(pokemonDto)).thenReturn(pokemonEntity);
        when(cardRepository.save(any())).thenReturn(pokemonEntity);
        when(cardMapper.toDto(pokemonEntity)).thenReturn(pokemonDto);

        int synced = cardService.syncSet("xy1");

        assertThat(synced).isEqualTo(1);
        verify(externalCardApiService).fetchSetCards("xy1");
    }

    @Test
    void saveOrUpdate_existingCard_updates() {
        String externalId = "xy1-1";
        pokemonDto.setExternalId(externalId);

        when(cardRepository.findByExternalId(externalId)).thenReturn(Optional.of(pokemonEntity));
        when(cardRepository.save(pokemonEntity)).thenReturn(pokemonEntity);
        when(cardMapper.toDto(pokemonEntity)).thenReturn(pokemonDto);

        CardDTO result = cardService.saveOrUpdate(pokemonDto);

        assertThat(result).isSameAs(pokemonDto);
        verify(cardRepository).save(pokemonEntity);
    }
}
