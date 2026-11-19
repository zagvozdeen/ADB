package ru.fitnessclub.domain;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToOne;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;

import java.time.Instant;

@Entity
@Table(name = "clients")
public class Client extends AuditedEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "clients_id_seq")
    @SequenceGenerator(name = "clients_id_seq", sequenceName = "clients_id_seq", allocationSize = 1)
    private Integer id;

    @Column(nullable = false)
    private String firstName;

    @Column(nullable = false)
    private String lastName;

    @OneToOne(mappedBy = "client", fetch = FetchType.LAZY,
        cascade = CascadeType.PERSIST, orphanRemoval = true)
    private Membership membership;

    @Override
    public Integer getId() {
        return id;
    }

    protected Client() {
    }

    public Client(String firstName, String lastName) {
        this.firstName = firstName;
        this.lastName = lastName;
    }

    public void issueMembership(Instant validFrom, Instant validUntil) {
        if (membership != null) {
            throw new IllegalStateException("Клиенту уже выдан абонемент");
        }
        membership = new Membership(this, validFrom, validUntil);
    }

    public void removeMembership() {
        if (membership != null) {
            membership.detachFromClient();
            membership = null;
        }
    }
}
