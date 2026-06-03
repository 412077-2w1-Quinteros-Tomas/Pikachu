package ar.edu.utn.frc.tup.piii.mappers;

import ar.edu.utn.frc.tup.piii.dtos.card.CardDTO;
import ar.edu.utn.frc.tup.piii.entities.CardEntity;
import ar.edu.utn.frc.tup.piii.enums.CardType;
import ar.edu.utn.frc.tup.piii.enums.PokemonStage;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class CardMapperTest {

    private CardMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = new CardMapper(new ObjectMapper());
    }

    @Test
    void toDto_mapsAllFields() {
        CardEntity entity = new CardEntity();
        entity.setExternalId("xy1-1");
        entity.setName("Bulbasaur");
        entity.setCardType(CardType.POKEMON);
        entity.setHp(60);
        entity.setStage(PokemonStage.BASIC);
        entity.setSetId("xy1");
        entity.setWeakness("Fire");
        entity.setRetreatCost(1);
        entity.setImageUrl("https://example.com/img.png");

        CardDTO dto = mapper.toDto(entity);

        assertThat(dto.getName()).isEqualTo("Bulbasaur");
        assertThat(dto.getExternalId()).isEqualTo("xy1-1");
        assertThat(dto.getCardType()).isEqualTo(CardType.POKEMON);
        assertThat(dto.getHp()).isEqualTo(60);
        assertThat(dto.getWeakness()).isEqualTo("Fire");
        assertThat(dto.getSetId()).isEqualTo("xy1");
    }

    @Test
    void toEntity_mapsAllFields() {
        CardDTO dto = new CardDTO();
        dto.setExternalId("xy1-2");
        dto.setName("Charmander");
        dto.setCardType(CardType.POKEMON);
        dto.setHp(50);
        dto.setStage(PokemonStage.BASIC);
        dto.setSetId("xy1");

        CardEntity entity = mapper.toEntity(dto);

        assertThat(entity.getName()).isEqualTo("Charmander");
        assertThat(entity.getExternalId()).isEqualTo("xy1-2");
        assertThat(entity.getCardType()).isEqualTo(CardType.POKEMON);
        assertThat(entity.getHp()).isEqualTo(50);
    }

    @Test
    void toDtoList_mapsList() {
        CardEntity e1 = new CardEntity();
        e1.setName("Card1");
        e1.setCardType(CardType.ENERGY);
        e1.setSetId("xy1");

        CardEntity e2 = new CardEntity();
        e2.setName("Card2");
        e2.setCardType(CardType.TRAINER);
        e2.setSetId("xy1");

        List<CardDTO> dtos = mapper.toDtoList(List.of(e1, e2));

        assertThat(dtos).hasSize(2);
        assertThat(dtos.get(0).getName()).isEqualTo("Card1");
        assertThat(dtos.get(1).getName()).isEqualTo("Card2");
    }

    @Test
    void updateEntity_updatesFields() {
        CardEntity entity = new CardEntity();
        entity.setName("Old Name");
        entity.setSetId("xy1");

        CardDTO dto = new CardDTO();
        dto.setName("New Name");
        dto.setHp(90);
        dto.setCardType(CardType.POKEMON);
        dto.setSetId("xy1");

        mapper.updateEntity(entity, dto);

        assertThat(entity.getName()).isEqualTo("New Name");
        assertThat(entity.getHp()).isEqualTo(90);
    }
}
