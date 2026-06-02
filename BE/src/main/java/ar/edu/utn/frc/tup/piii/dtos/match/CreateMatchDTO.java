package ar.edu.utn.frc.tup.piii.dtos.match;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateMatchDTO {

    @NotBlank
    private String name;

    @NotBlank
    private String player1;

    private UUID deckId;
}
