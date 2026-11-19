package ru.fitnessclub.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.fitnessclub.domain.TrainingSession;

public interface TrainingSessionRepository extends JpaRepository<TrainingSession, Integer> {
}
