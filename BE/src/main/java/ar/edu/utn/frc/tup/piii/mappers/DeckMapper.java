package ar.edu.utn.frc.tup.piii.mappers;

import ar.edu.utn.frc.tup.piii.dtos.deck.DeckDTO;
import ar.edu.utn.frc.tup.piii.entities.DeckEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class DeckMapper {

    private final CardMapper cardMapper;

    public DeckDTO toDto(DeckEntity entity) {
        DeckDTO dto = new DeckDTO();
        dto.setId(entity.getId());
        dto.setName(entity.getName());
        dto.setDescription(entity.getDescription());

        List<DeckDTO.DeckCardItemDTO> items = entity.getCards().stream()
                .map(dc -> new DeckDTO.DeckCardItemDTO(
                        dc.getId(),
                        cardMapper.toDto(dc.getCard()),
                        dc.getQuantity()))
                .toList();

        dto.setCards(items);
        dto.setTotalCards(items.stream().mapToInt(DeckDTO.DeckCardItemDTO::getQuantity).sum());
        return dto;
    }

    public List<DeckDTO> toDtoList(List<DeckEntity> entities) {
        return entities.stream().map(this::toDto).toList();
    }
}
