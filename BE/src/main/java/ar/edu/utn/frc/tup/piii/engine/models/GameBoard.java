package ar.edu.utn.frc.tup.piii.engine.models;

import ar.edu.utn.frc.tup.piii.enums.GamePhase;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GameBoard {

    private String matchId;
    private PlayerBoard player1Board;
    private PlayerBoard player2Board;
    private GamePhase phase;
    private String currentPlayerId;
    private int turnNumber;
    private String winnerId;
    private List<String> actionLog = new ArrayList<>();

    public PlayerBoard getBoardFor(String playerId) {
        if (player1Board != null && playerId.equals(player1Board.getPlayerId())) return player1Board;
        if (player2Board != null && playerId.equals(player2Board.getPlayerId())) return player2Board;
        return null;
    }

    public PlayerBoard getOpponentBoard(String playerId) {
        if (player1Board != null && playerId.equals(player1Board.getPlayerId())) return player2Board;
        return player1Board;
    }

    public boolean isFinished() {
        return phase == GamePhase.FINISHED;
    }

    public void log(String entry) {
        if (actionLog == null) actionLog = new ArrayList<>();
        actionLog.add(entry);
    }
}
