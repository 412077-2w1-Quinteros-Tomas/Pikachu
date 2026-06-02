package ar.edu.utn.frc.tup.piii.controllers;

import ar.edu.utn.frc.tup.piii.dtos.card.CardDTO;
import ar.edu.utn.frc.tup.piii.dtos.card.CardFilterDTO;
import ar.edu.utn.frc.tup.piii.enums.CardType;
import ar.edu.utn.frc.tup.piii.enums.EnergyType;
import ar.edu.utn.frc.tup.piii.enums.PokemonStage;
import ar.edu.utn.frc.tup.piii.services.CardService;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/cards")
@RequiredArgsConstructor
public class CardController {

    private final CardService cardService;

    @GetMapping
    public ResponseEntity<List<CardDTO>> getCards(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) CardType cardType,
            @RequestParam(required = false) EnergyType type,
            @RequestParam(required = false) PokemonStage stage,
            @RequestParam(required = false) String setId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        CardFilterDTO filter = new CardFilterDTO();
        filter.setName(name);
        filter.setCardType(cardType);
        filter.setType(type);
        filter.setStage(stage);
        filter.setSetId(setId);
        filter.setPage(page);
        filter.setSize(size);

        return ResponseEntity.ok(cardService.getCards(filter));
    }

    @GetMapping("/{id}")
    public ResponseEntity<CardDTO> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(cardService.getById(id));
    }

    @GetMapping("/search")
    public ResponseEntity<List<CardDTO>> search(@RequestParam String name) {
        return ResponseEntity.ok(cardService.searchByName(name));
    }

    @PostMapping("/sync")
    public ResponseEntity<Map<String, Integer>> sync(@RequestBody SyncRequest request) {
        int synced = cardService.syncSet(request.setId());
        return ResponseEntity.ok(Map.of("synced", synced));
    }

    record SyncRequest(@NotBlank String setId) {}
}
