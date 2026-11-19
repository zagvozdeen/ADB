package ru.fitnessclub.repository;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import ru.fitnessclub.domain.Client;

import java.util.Optional;

public interface ClientRepository extends JpaRepository<Client, Integer> {
    @EntityGraph(attributePaths = "membership")
    Optional<Client> findWithMembershipById(Integer id);
}
