package ar.edu.utn.frc.tup.piii.repositories;

import ar.edu.utn.frc.tup.piii.entities.CardEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface CardRepository extends JpaRepository<CardEntity, UUID> {

    Optional<CardEntity> findByExternalId(String externalId);

    boolean existsByExternalId(String externalId);

    List<CardEntity> findBySetId(String setId);

    List<CardEntity> findByNameContainingIgnoreCase(String name);
}
