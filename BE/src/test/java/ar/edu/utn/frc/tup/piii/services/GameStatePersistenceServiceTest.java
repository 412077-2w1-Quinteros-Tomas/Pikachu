package ar.edu.utn.frc.tup.piii.services;

import ar.edu.utn.frc.tup.piii.dtos.match.MatchStateDTO;
import ar.edu.utn.frc.tup.piii.entities.GameStateEntity;
import ar.edu.utn.frc.tup.piii.entities.MatchEntity;
import ar.edu.utn.frc.tup.piii.repositories.GameStateRepository;
import ar.edu.utn.frc.tup.piii.repositories.MatchRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GameStatePersistenceServiceTest {

    @Mock private GameStateRepository gameStateRepository;
    @Mock private MatchRepository matchRepository;

    @InjectMocks private GameStatePersistenceService service;

    @Test
    void saveState_existingState_updatesAndSaves() {
        UUID matchId = UUID.randomUUID();
        GameStateEntity existing = new GameStateEntity();
        existing.setStateJson("{}");

        when(gameStateRepository.findByMatchId(matchId)).thenReturn(Optional.of(existing));
        when(gameStateRepository.save(existing)).thenReturn(existing);

        GameStateEntity result = service.saveState(matchId, "{\"turn\":1}", 1, "player1");

        assertThat(result.getStateJson()).isEqualTo("{\"turn\":1}");
        assertThat(result.getTurnNumber()).isEqualTo(1);
        assertThat(result.getCurrentPlayer()).isEqualTo("player1");
        verify(gameStateRepository).save(existing);
    }

    @Test
    void saveState_noExistingState_createsNew() {
        UUID matchId = UUID.randomUUID();
        MatchEntity match = new MatchEntity();

        when(gameStateRepository.findByMatchId(matchId)).thenReturn(Optional.empty());
        when(matchRepository.findById(matchId)).thenReturn(Optional.of(match));
        when(gameStateRepository.save(any(GameStateEntity.class))).thenAnswer(inv -> inv.getArgument(0));

        GameStateEntity result = service.saveState(matchId, "{}", 0, "player1");

        assertThat(result).isNotNull();
        assertThat(result.getMatch()).isSameAs(match);
    }

    @Test
    void loadState_returnsOptional() {
        UUID matchId = UUID.randomUUID();
        GameStateEntity entity = new GameStateEntity();
        when(gameStateRepository.findByMatchId(matchId)).thenReturn(Optional.of(entity));

        Optional<GameStateEntity> result = service.loadState(matchId);

        assertThat(result).isPresent().contains(entity);
    }

    @Test
    void loadState_notFound_returnsEmpty() {
        UUID matchId = UUID.randomUUID();
        when(gameStateRepository.findByMatchId(matchId)).thenReturn(Optional.empty());

        Optional<GameStateEntity> result = service.loadState(matchId);

        assertThat(result).isEmpty();
    }

    @Test
    void getStateDto_notFound_returnsNull() {
        UUID matchId = UUID.randomUUID();
        when(gameStateRepository.findByMatchId(matchId)).thenReturn(Optional.empty());

        MatchStateDTO result = service.getStateDto(matchId);

        assertThat(result).isNull();
    }

    @Test
    void getStateDto_found_returnsDto() {
        UUID matchId = UUID.randomUUID();
        MatchEntity match = new MatchEntity();
        match.setStatus("IN_PROGRESS");
        GameStateEntity state = new GameStateEntity();
        state.setMatch(match);
        state.setStateJson("{}");
        state.setTurnNumber(3);
        state.setCurrentPlayer("p1");

        when(gameStateRepository.findByMatchId(matchId)).thenReturn(Optional.of(state));

        MatchStateDTO result = service.getStateDto(matchId);

        assertThat(result).isNotNull();
        assertThat(result.getTurnNumber()).isEqualTo(3);
        assertThat(result.getCurrentPlayer()).isEqualTo("p1");
    }
}
