package ru.fitnessclub.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;

import java.time.Instant;

@Entity
@Table(name = "training_sessions")
public class TrainingSession extends AuditedEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "training_sessions_id_seq")
    @SequenceGenerator(name = "training_sessions_id_seq", sequenceName = "training_sessions_id_seq", allocationSize = 1)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "trainer_id", nullable = false)
    private Trainer trainer;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    private Instant startsAt;

    @Override
    public Integer getId() {
        return id;
    }

    protected TrainingSession() {
    }

    TrainingSession(Trainer trainer, String title, Instant startsAt) {
        this.trainer = trainer;
        this.title = title;
        this.startsAt = startsAt;
    }

    public Trainer getTrainer() {
        return trainer;
    }

    public String getTitle() {
        return title;
    }

    public Instant getStartsAt() {
        return startsAt;
    }
}
