package ar.edu.utn.frc.tup.piii.dtos.deck;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DeckValidationResultDTO {

    private boolean valid;
    private List<String> errors;
    private int totalCards;
}
