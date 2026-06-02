package ar.edu.utn.frc.tup.piii.services;

import ar.edu.utn.frc.tup.piii.dtos.match.CreateMatchDTO;
import ar.edu.utn.frc.tup.piii.dtos.match.MatchDTO;
import ar.edu.utn.frc.tup.piii.entities.MatchEntity;
import ar.edu.utn.frc.tup.piii.exceptions.EntityNotFoundException;
import ar.edu.utn.frc.tup.piii.exceptions.InvalidActionException;
import ar.edu.utn.frc.tup.piii.mappers.MatchMapper;
import ar.edu.utn.frc.tup.piii.repositories.MatchRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class MatchService {

    private final MatchRepository matchRepository;
    private final MatchMapper matchMapper;

    public List<MatchDTO> getAllMatches() {
        return matchMapper.toDtoList(matchRepository.findAllByOrderByCreatedAtDesc());
    }

    public List<MatchDTO> getWaitingMatches() {
        return matchMapper.toDtoList(matchRepository.findAllByStatusOrderByCreatedAtDesc("WAITING"));
    }

    public MatchDTO getMatchById(UUID id) {
        return matchMapper.toDto(findOrThrow(id));
    }

    @Transactional
    public MatchDTO createMatch(CreateMatchDTO dto) {
        MatchEntity match = new MatchEntity();
        match.setName(dto.getName());
        match.setPlayer1(dto.getPlayer1());
        match.setDeck1Id(dto.getDeckId());
        match.setStatus("WAITING");
        return matchMapper.toDto(matchRepository.save(match));
    }

    @Transactional
    public MatchDTO joinMatch(UUID id, String player2, UUID deck2Id) {
        MatchEntity match = findOrThrow(id);
        if (!"WAITING".equals(match.getStatus())) {
            throw new InvalidActionException("Match is not available to join (status: " + match.getStatus() + ")");
        }
        if (match.getPlayer1().equals(player2)) {
            throw new InvalidActionException("You cannot join your own match");
        }
        match.setPlayer2(player2);
        match.setDeck2Id(deck2Id);
        match.setStatus("IN_PROGRESS");
        return matchMapper.toDto(matchRepository.save(match));
    }

    @Transactional
    public MatchDTO finishMatch(UUID id) {
        MatchEntity match = findOrThrow(id);
        match.setStatus("FINISHED");
        return matchMapper.toDto(matchRepository.save(match));
    }

    private MatchEntity findOrThrow(UUID id) {
        return matchRepository.findById(id)
                .orElseThrow(() -> EntityNotFoundException.of("Match", id));
    }
}
