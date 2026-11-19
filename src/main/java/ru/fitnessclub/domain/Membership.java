package ru.fitnessclub.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;

import java.time.Instant;

@Entity
@Table(name = "memberships")
public class Membership extends AuditedEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "memberships_id_seq")
    @SequenceGenerator(name = "memberships_id_seq", sequenceName = "memberships_id_seq", allocationSize = 1)
    private Integer id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "client_id", nullable = false, unique = true)
    private Client client;

    @Column(nullable = false)
    private Instant validFrom;

    @Column(nullable = false)
    private Instant validUntil;

    @Override
    public Integer getId() {
        return id;
    }

    protected Membership() {
    }

    Membership(Client client, Instant validFrom, Instant validUntil) {
        if (!validUntil.isAfter(validFrom)) {
            throw new IllegalArgumentException("validUntil должен быть позже validFrom");
        }
        this.client = client;
        this.validFrom = validFrom;
        this.validUntil = validUntil;
    }

    void detachFromClient() {
        client = null;
    }

    public Instant getValidFrom() {
        return validFrom;
    }

    public Instant getValidUntil() {
        return validUntil;
    }
}
