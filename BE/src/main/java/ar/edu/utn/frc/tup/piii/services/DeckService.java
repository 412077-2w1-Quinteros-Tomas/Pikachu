package ar.edu.utn.frc.tup.piii.services;

import ar.edu.utn.frc.tup.piii.dtos.deck.CreateDeckDTO;
import ar.edu.utn.frc.tup.piii.dtos.deck.DeckDTO;
import ar.edu.utn.frc.tup.piii.dtos.deck.DeckValidationResultDTO;
import ar.edu.utn.frc.tup.piii.entities.CardEntity;
import ar.edu.utn.frc.tup.piii.entities.DeckCardEntity;
import ar.edu.utn.frc.tup.piii.entities.DeckEntity;
import ar.edu.utn.frc.tup.piii.exceptions.EntityNotFoundException;
import ar.edu.utn.frc.tup.piii.mappers.DeckMapper;
import ar.edu.utn.frc.tup.piii.repositories.CardRepository;
import ar.edu.utn.frc.tup.piii.repositories.DeckRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class DeckService {

    private final DeckRepository deckRepository;
    private final CardRepository cardRepository;
    private final DeckMapper deckMapper;
    private final DeckValidationService validationService;

    public List<DeckDTO> getAllDecks() {
        return deckRepository.findAllByOrderByCreatedAtDesc().stream()
                .map(this::toValidatedDto)
                .toList();
    }

    public DeckDTO getDeckById(UUID id) {
        return toValidatedDto(findOrThrow(id));
    }

    @Transactional
    public DeckDTO createDeck(CreateDeckDTO dto) {
        DeckEntity deck = new DeckEntity();
        deck.setName(dto.getName());
        deck.setDescription(dto.getDescription());
        deck = deckRepository.save(deck);
        applyCards(deck, dto);
        return toValidatedDto(deckRepository.save(deck));
    }

    @Transactional
    public DeckDTO updateDeck(UUID id, CreateDeckDTO dto) {
        DeckEntity deck = findOrThrow(id);
        deck.setName(dto.getName());
        deck.setDescription(dto.getDescription());
        deck.getCards().clear();
        applyCards(deck, dto);
        return toValidatedDto(deckRepository.save(deck));
    }

    private DeckDTO toValidatedDto(DeckEntity deck) {
        DeckDTO dto = deckMapper.toDto(deck);
        dto.setValid(validationService.validate(deck).isValid());
        return dto;
    }

    @Transactional
    public void deleteDeck(UUID id) {
        if (!deckRepository.existsById(id)) {
            throw EntityNotFoundException.of("Deck", id);
        }
        deckRepository.deleteById(id);
    }

    public DeckValidationResultDTO validateDeck(UUID id) {
        DeckEntity deck = findOrThrow(id);
        return validationService.validate(deck);
    }

    private DeckEntity findOrThrow(UUID id) {
        return deckRepository.findById(id)
                .orElseThrow(() -> EntityNotFoundException.of("Deck", id));
    }

    private void applyCards(DeckEntity deck, CreateDeckDTO dto) {
        for (CreateDeckDTO.CardEntryDTO entry : dto.getCards()) {
            if (entry.getQuantity() <= 0) {
                continue;
            }
            CardEntity card = cardRepository.findById(entry.getCardId())
                    .orElseThrow(() -> EntityNotFoundException.of("Card", entry.getCardId()));
            DeckCardEntity dc = new DeckCardEntity();
            dc.setDeck(deck);
            dc.setCard(card);
            dc.setQuantity(entry.getQuantity());
            deck.getCards().add(dc);
        }
    }
}
