package ar.edu.utn.frc.tup.piii.repositories;

import ar.edu.utn.frc.tup.piii.entities.MatchEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface MatchRepository extends JpaRepository<MatchEntity, UUID> {

    List<MatchEntity> findAllByOrderByCreatedAtDesc();

    List<MatchEntity> findAllByStatusOrderByCreatedAtDesc(String status);
}
