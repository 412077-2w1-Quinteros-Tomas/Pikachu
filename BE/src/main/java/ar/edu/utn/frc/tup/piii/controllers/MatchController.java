package ar.edu.utn.frc.tup.piii.controllers;

import ar.edu.utn.frc.tup.piii.dtos.match.CreateMatchDTO;
import ar.edu.utn.frc.tup.piii.dtos.match.MatchDTO;
import ar.edu.utn.frc.tup.piii.dtos.match.MatchStateDTO;
import ar.edu.utn.frc.tup.piii.services.GameStatePersistenceService;
import ar.edu.utn.frc.tup.piii.services.MatchService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/matches")
@RequiredArgsConstructor
public class MatchController {

    private final MatchService matchService;
    private final GameStatePersistenceService gameStatePersistenceService;

    @GetMapping
    public ResponseEntity<List<MatchDTO>> getAllMatches(
            @RequestParam(required = false) String status) {
        List<MatchDTO> matches = "WAITING".equalsIgnoreCase(status)
                ? matchService.getWaitingMatches()
                : matchService.getAllMatches();
        return ResponseEntity.ok(matches);
    }

    @GetMapping("/{id}")
    public ResponseEntity<MatchDTO> getMatchById(@PathVariable UUID id) {
        return ResponseEntity.ok(matchService.getMatchById(id));
    }

    @PostMapping
    public ResponseEntity<MatchDTO> createMatch(@Valid @RequestBody CreateMatchDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(matchService.createMatch(dto));
    }

    @PostMapping("/{id}/join")
    public ResponseEntity<MatchDTO> joinMatch(
            @PathVariable UUID id,
            @RequestBody JoinRequest request) {
        return ResponseEntity.ok(matchService.joinMatch(id, request.player2(), request.deckId()));
    }

    @GetMapping("/{id}/state")
    public ResponseEntity<MatchStateDTO> getMatchState(@PathVariable UUID id) {
        MatchStateDTO state = gameStatePersistenceService.getStateDto(id);
        return state != null ? ResponseEntity.ok(state) : ResponseEntity.notFound().build();
    }

    record JoinRequest(@NotBlank String player2, UUID deckId) {}
}
