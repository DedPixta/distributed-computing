package dev.makos.publisher.repository;

import dev.makos.publisher.model.entity.Creator;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CreatorRepository extends JpaRepository<Creator, Long> {

    boolean existsByLogin(String login);

    Optional<Creator> findCreatorByLogin(String login);

}
