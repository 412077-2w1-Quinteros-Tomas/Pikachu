package ar.edu.utn.frc.tup.piii.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "game_states")
@Getter
@Setter
@NoArgsConstructor
public class GameStateEntity extends BaseEntity {

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "match_id", nullable = false, unique = true)
    private MatchEntity match;

    @Column(columnDefinition = "TEXT")
    private String stateJson;

    private int turnNumber;

    private String currentPlayer;
}
