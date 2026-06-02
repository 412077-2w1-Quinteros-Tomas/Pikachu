package ar.edu.utn.frc.tup.piii.services;

import ar.edu.utn.frc.tup.piii.dtos.card.CardDTO;
import ar.edu.utn.frc.tup.piii.dtos.card.CardFilterDTO;
import ar.edu.utn.frc.tup.piii.entities.CardEntity;
import ar.edu.utn.frc.tup.piii.exceptions.EntityNotFoundException;
import ar.edu.utn.frc.tup.piii.mappers.CardMapper;
import ar.edu.utn.frc.tup.piii.repositories.CardRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CardService {

    private final CardRepository cardRepository;
    private final CardMapper cardMapper;
    private final ExternalCardApiService externalCardApiService;

    public CardDTO getById(UUID id) {
        CardEntity entity = cardRepository.findById(id)
                .orElseThrow(() -> EntityNotFoundException.of("Card", id));
        return cardMapper.toDto(entity);
    }

    public List<CardDTO> getCards(CardFilterDTO filter) {
        String setId = filter.getSetId() != null ? filter.getSetId() : "xy1";
        List<CardEntity> cards = cardRepository.findBySetId(setId);

        return cards.stream()
                .filter(c -> filter.getName() == null
                        || c.getName().toLowerCase().contains(filter.getName().toLowerCase()))
                .filter(c -> filter.getCardType() == null
                        || filter.getCardType().equals(c.getCardType()))
                .filter(c -> filter.getStage() == null
                        || filter.getStage().equals(c.getStage()))
                .skip((long) filter.getPage() * filter.getSize())
                .limit(filter.getSize())
                .map(cardMapper::toDto)
                .toList();
    }

    public List<CardDTO> searchByName(String name) {
        return cardMapper.toDtoList(cardRepository.findByNameContainingIgnoreCase(name));
    }

    public CardDTO save(CardDTO dto) {
        CardEntity entity = cardMapper.toEntity(dto);
        return cardMapper.toDto(cardRepository.save(entity));
    }

    public CardDTO saveOrUpdate(CardDTO dto) {
        return cardRepository.findByExternalId(dto.getExternalId())
                .map(existing -> {
                    cardMapper.updateEntity(existing, dto);
                    return cardMapper.toDto(cardRepository.save(existing));
                })
                .orElseGet(() -> save(dto));
    }

    public int syncSet(String setId) {
        List<CardDTO> cards = externalCardApiService.fetchSetCards(setId);
        cards.forEach(this::saveOrUpdate);
        return cards.size();
    }
}
