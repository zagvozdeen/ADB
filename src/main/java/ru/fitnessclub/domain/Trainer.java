package ru.fitnessclub.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "trainers")
public class Trainer extends AuditedEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "trainers_id_seq")
    @SequenceGenerator(name = "trainers_id_seq", sequenceName = "trainers_id_seq", allocationSize = 1)
    private Integer id;

    @Column(nullable = false)
    private String firstName;

    @Column(nullable = false)
    private String lastName;

    @OneToMany(mappedBy = "trainer", fetch = FetchType.LAZY)
    private List<TrainingSession> sessions = new ArrayList<>();

    @Override
    public Integer getId() {
        return id;
    }

    protected Trainer() {
    }

    public Trainer(String firstName, String lastName) {
        this.firstName = firstName;
        this.lastName = lastName;
    }

    public TrainingSession scheduleSession(String title, Instant startsAt) {
        TrainingSession session = new TrainingSession(this, title, startsAt);
        sessions.add(session);
        return session;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public List<TrainingSession> getSessions() {
        return List.copyOf(sessions);
    }
}
