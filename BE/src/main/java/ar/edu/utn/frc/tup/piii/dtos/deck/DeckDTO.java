package ar.edu.utn.frc.tup.piii.dtos.deck;

import ar.edu.utn.frc.tup.piii.dtos.card.CardDTO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DeckDTO {

    private UUID id;
    private String name;
    private String description;
    private List<DeckCardItemDTO> cards;
    private int totalCards;
    private boolean valid;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DeckCardItemDTO {
        private UUID id;
        private CardDTO card;
        private int quantity;
    }
}
