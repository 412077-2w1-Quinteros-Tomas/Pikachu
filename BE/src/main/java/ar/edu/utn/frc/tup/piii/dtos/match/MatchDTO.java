package ar.edu.utn.frc.tup.piii.dtos.match;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MatchDTO {

    private UUID id;
    private String name;
    private String player1;
    private String player2;
    private UUID deck1Id;
    private UUID deck2Id;
    private String status;
    private LocalDateTime createdAt;
}
