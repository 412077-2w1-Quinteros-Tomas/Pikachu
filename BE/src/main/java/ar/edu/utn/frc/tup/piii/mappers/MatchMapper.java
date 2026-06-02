package ar.edu.utn.frc.tup.piii.mappers;

import ar.edu.utn.frc.tup.piii.dtos.match.MatchDTO;
import ar.edu.utn.frc.tup.piii.entities.MatchEntity;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class MatchMapper {

    public MatchDTO toDto(MatchEntity entity) {
        MatchDTO dto = new MatchDTO();
        dto.setId(entity.getId());
        dto.setName(entity.getName());
        dto.setPlayer1(entity.getPlayer1());
        dto.setPlayer2(entity.getPlayer2());
        dto.setDeck1Id(entity.getDeck1Id());
        dto.setDeck2Id(entity.getDeck2Id());
        dto.setStatus(entity.getStatus());
        dto.setCreatedAt(entity.getCreatedAt());
        return dto;
    }

    public List<MatchDTO> toDtoList(List<MatchEntity> entities) {
        return entities.stream().map(this::toDto).toList();
    }
}
