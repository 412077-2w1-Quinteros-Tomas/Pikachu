package ar.edu.utn.frc.tup.piii.services;

import ar.edu.utn.frc.tup.piii.dtos.match.CreateMatchDTO;
import ar.edu.utn.frc.tup.piii.dtos.match.MatchDTO;
import ar.edu.utn.frc.tup.piii.entities.MatchEntity;
import ar.edu.utn.frc.tup.piii.exceptions.EntityNotFoundException;
import ar.edu.utn.frc.tup.piii.exceptions.InvalidActionException;
import ar.edu.utn.frc.tup.piii.mappers.MatchMapper;
import ar.edu.utn.frc.tup.piii.repositories.MatchRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MatchServiceTest {

    @Mock private MatchRepository matchRepository;
    @Mock private MatchMapper matchMapper;
    @InjectMocks private MatchService matchService;

    @Test
    void createMatch_savesAndReturnsMapped() {
        CreateMatchDTO dto = new CreateMatchDTO("Test Match", "Player1", UUID.randomUUID());
        MatchEntity saved = new MatchEntity();
        MatchDTO expected = new MatchDTO();

        when(matchRepository.save(any(MatchEntity.class))).thenReturn(saved);
        when(matchMapper.toDto(saved)).thenReturn(expected);

        MatchDTO result = matchService.createMatch(dto);

        assertThat(result).isSameAs(expected);
        verify(matchRepository).save(any(MatchEntity.class));
    }

    @Test
    void getMatchById_notFound_throws() {
        UUID id = UUID.randomUUID();
        when(matchRepository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> matchService.getMatchById(id))
                .isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    void joinMatch_alreadyInProgress_throws() {
        UUID id = UUID.randomUUID();
        MatchEntity match = new MatchEntity();
        match.setPlayer1("Player1");
        match.setStatus("IN_PROGRESS");

        when(matchRepository.findById(id)).thenReturn(Optional.of(match));

        assertThatThrownBy(() -> matchService.joinMatch(id, "Player2", null))
                .isInstanceOf(InvalidActionException.class)
                .hasMessageContaining("not available");
    }

    @Test
    void joinMatch_samePlayer_throws() {
        UUID id = UUID.randomUUID();
        MatchEntity match = new MatchEntity();
        match.setPlayer1("Player1");
        match.setStatus("WAITING");

        when(matchRepository.findById(id)).thenReturn(Optional.of(match));

        assertThatThrownBy(() -> matchService.joinMatch(id, "Player1", null))
                .isInstanceOf(InvalidActionException.class)
                .hasMessageContaining("own match");
    }

    @Test
    void joinMatch_success_setsInProgress() {
        UUID id = UUID.randomUUID();
        MatchEntity match = new MatchEntity();
        match.setPlayer1("Player1");
        match.setStatus("WAITING");
        MatchDTO expected = new MatchDTO();

        when(matchRepository.findById(id)).thenReturn(Optional.of(match));
        when(matchRepository.save(match)).thenReturn(match);
        when(matchMapper.toDto(match)).thenReturn(expected);

        MatchDTO result = matchService.joinMatch(id, "Player2", null);

        assertThat(result).isSameAs(expected);
        assertThat(match.getStatus()).isEqualTo("IN_PROGRESS");
        assertThat(match.getPlayer2()).isEqualTo("Player2");
    }

    @Test
    void getWaitingMatches_filtersCorrectly() {
        when(matchRepository.findAllByStatusOrderByCreatedAtDesc("WAITING")).thenReturn(List.of());
        when(matchMapper.toDtoList(List.of())).thenReturn(List.of());

        List<MatchDTO> result = matchService.getWaitingMatches();

        assertThat(result).isEmpty();
        verify(matchRepository).findAllByStatusOrderByCreatedAtDesc("WAITING");
    }
}
