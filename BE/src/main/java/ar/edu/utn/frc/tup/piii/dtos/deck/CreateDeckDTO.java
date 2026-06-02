package ar.edu.utn.frc.tup.piii.dtos.deck;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateDeckDTO {

    @NotBlank
    private String name;

    private String description;

    private List<CardEntryDTO> cards = new ArrayList<>();

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CardEntryDTO {
        private UUID cardId;
        private int quantity;
    }
}
