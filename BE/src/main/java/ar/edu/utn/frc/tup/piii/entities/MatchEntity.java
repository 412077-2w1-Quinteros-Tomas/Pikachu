package ar.edu.utn.frc.tup.piii.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Entity
@Table(name = "matches")
@Getter
@Setter
@NoArgsConstructor
public class MatchEntity extends BaseEntity {

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String player1;

    private String player2;

    private UUID deck1Id;

    private UUID deck2Id;

    /** WAITING | IN_PROGRESS | FINISHED */
    @Column(nullable = false)
    private String status = "WAITING";
}
