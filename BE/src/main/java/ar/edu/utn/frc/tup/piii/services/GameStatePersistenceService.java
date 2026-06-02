package ar.edu.utn.frc.tup.piii.services;

import ar.edu.utn.frc.tup.piii.dtos.match.MatchStateDTO;
import ar.edu.utn.frc.tup.piii.entities.GameStateEntity;
import ar.edu.utn.frc.tup.piii.entities.MatchEntity;
import ar.edu.utn.frc.tup.piii.exceptions.EntityNotFoundException;
import ar.edu.utn.frc.tup.piii.repositories.GameStateRepository;
import ar.edu.utn.frc.tup.piii.repositories.MatchRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class GameStatePersistenceService {

    private final GameStateRepository gameStateRepository;
    private final MatchRepository matchRepository;

    @Transactional
    public GameStateEntity saveState(UUID matchId, String stateJson, int turnNumber, String currentPlayer) {
        GameStateEntity state = gameStateRepository.findByMatchId(matchId)
                .orElseGet(() -> {
                    MatchEntity match = matchRepository.findById(matchId)
                            .orElseThrow(() -> EntityNotFoundException.of("Match", matchId));
                    GameStateEntity newState = new GameStateEntity();
                    newState.setMatch(match);
                    return newState;
                });
        state.setStateJson(stateJson);
        state.setTurnNumber(turnNumber);
        state.setCurrentPlayer(currentPlayer);
        return gameStateRepository.save(state);
    }

    public Optional<GameStateEntity> loadState(UUID matchId) {
        return gameStateRepository.findByMatchId(matchId);
    }

    public MatchStateDTO getStateDto(UUID matchId) {
        return gameStateRepository.findByMatchId(matchId)
                .map(s -> new MatchStateDTO(
                        matchId,
                        s.getMatch().getStatus(),
                        s.getStateJson(),
                        s.getTurnNumber(),
                        s.getCurrentPlayer()))
                .orElse(null);
    }
}
