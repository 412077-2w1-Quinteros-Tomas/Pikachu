package ar.edu.utn.frc.tup.piii.mappers;

import ar.edu.utn.frc.tup.piii.dtos.card.CardDTO;
import ar.edu.utn.frc.tup.piii.entities.CardEntity;
import ar.edu.utn.frc.tup.piii.enums.EnergyType;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

@Component
@RequiredArgsConstructor
public class CardMapper {

    private final ObjectMapper objectMapper;

    public CardDTO toDto(CardEntity entity) {
        CardDTO dto = new CardDTO();
        dto.setId(entity.getId() != null ? entity.getId().toString() : null);
        dto.setExternalId(entity.getExternalId());
        dto.setName(entity.getName());
        dto.setCardType(entity.getCardType());
        dto.setHp(entity.getHp());
        dto.setStage(entity.getStage());
        dto.setWeakness(entity.getWeakness());
        dto.setResistance(entity.getResistance());
        dto.setRetreatCost(entity.getRetreatCost());
        dto.setImageUrl(entity.getImageUrl());
        dto.setRarity(entity.getRarity());
        dto.setCardNumber(entity.getCardNumber());
        dto.setSetId(entity.getSetId());
        dto.setTypes(deserialize(entity.getTypes(), new TypeReference<List<EnergyType>>() {}));
        dto.setAttacks(deserialize(entity.getAttacks(), new TypeReference<List<CardDTO.AttackDTO>>() {}));
        dto.setAbilities(deserialize(entity.getAbilities(), new TypeReference<List<CardDTO.AbilityDTO>>() {}));
        return dto;
    }

    public List<CardDTO> toDtoList(List<CardEntity> entities) {
        return entities.stream().map(this::toDto).toList();
    }

    public CardEntity toEntity(CardDTO dto) {
        CardEntity entity = new CardEntity();
        entity.setExternalId(dto.getExternalId());
        entity.setName(dto.getName());
        entity.setCardType(dto.getCardType());
        entity.setHp(dto.getHp());
        entity.setStage(dto.getStage());
        entity.setWeakness(dto.getWeakness());
        entity.setResistance(dto.getResistance());
        entity.setRetreatCost(dto.getRetreatCost());
        entity.setImageUrl(dto.getImageUrl());
        entity.setRarity(dto.getRarity());
        entity.setCardNumber(dto.getCardNumber());
        entity.setSetId(dto.getSetId() != null ? dto.getSetId() : "xy1");
        entity.setTypes(serialize(dto.getTypes()));
        entity.setAttacks(serialize(dto.getAttacks()));
        entity.setAbilities(serialize(dto.getAbilities()));
        return entity;
    }

    public void updateEntity(CardEntity entity, CardDTO dto) {
        entity.setName(dto.getName());
        entity.setCardType(dto.getCardType());
        entity.setHp(dto.getHp());
        entity.setStage(dto.getStage());
        entity.setWeakness(dto.getWeakness());
        entity.setResistance(dto.getResistance());
        entity.setRetreatCost(dto.getRetreatCost());
        entity.setImageUrl(dto.getImageUrl());
        entity.setRarity(dto.getRarity());
        entity.setCardNumber(dto.getCardNumber());
        entity.setTypes(serialize(dto.getTypes()));
        entity.setAttacks(serialize(dto.getAttacks()));
        entity.setAbilities(serialize(dto.getAbilities()));
    }

    private <T> T deserialize(String json, TypeReference<T> type) {
        if (json == null || json.isBlank()) {
            return null;
        }
        try {
            return objectMapper.readValue(json, type);
        } catch (JsonProcessingException e) {
            return null;
        }
    }

    private String serialize(Object value) {
        if (value == null || (value instanceof List<?> list && list.isEmpty())) {
            return null;
        }
        try {
            return objectMapper.writeValueAsString(value);
        } catch (JsonProcessingException e) {
            return null;
        }
    }
}
