package ru.fitnessclub.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.fitnessclub.domain.Membership;

import java.time.Instant;
import java.util.Optional;

public interface MembershipRepository extends JpaRepository<Membership, Integer> {
    @Query(value = """
        SELECT m.* FROM memberships m
        WHERE m.client_id = :clientId
          AND m.valid_from <= :at
          AND m.valid_until > :at
        """, nativeQuery = true)
    Optional<Membership> findActiveAt(@Param("clientId") int clientId, @Param("at") Instant at);
}
