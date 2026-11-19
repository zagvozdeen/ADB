package ru.fitnessclub.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.fitnessclub.domain.Trainer;

import java.util.Optional;

public interface TrainerRepository extends JpaRepository<Trainer, Integer> {
    @Query("""
        select distinct t from Trainer t
        left join fetch t.sessions
        where t.id = :trainerId
        """)
    Optional<Trainer> findWithSessions(@Param("trainerId") int trainerId);
}
