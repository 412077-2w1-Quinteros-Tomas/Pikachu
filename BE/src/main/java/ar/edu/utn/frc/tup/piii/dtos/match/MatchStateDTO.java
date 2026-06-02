package ar.edu.utn.frc.tup.piii.dtos.match;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MatchStateDTO {

    private UUID matchId;
    private String status;
    private String stateJson;
    private int turnNumber;
    private String currentPlayer;
}
