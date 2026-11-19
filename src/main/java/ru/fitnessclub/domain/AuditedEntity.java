package ru.fitnessclub.domain;

import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.proxy.HibernateProxy;

import java.time.Instant;
import java.util.Objects;

@MappedSuperclass
public abstract class AuditedEntity {
    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(nullable = false)
    private Instant updatedAt;

    public abstract Integer getId();

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    @Override
    public final boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof AuditedEntity entity) || entityType(this) != entityType(entity)) {
            return false;
        }
        Integer thisId = identifier(this);
        return thisId != null && thisId.equals(identifier(entity));
    }

    @Override
    public final int hashCode() {
        return 31 * entityType(this).hashCode() + Objects.hashCode(identifier(this));
    }

    private static Class<?> entityType(AuditedEntity entity) {
        if (entity instanceof HibernateProxy proxy) {
            return proxy.getHibernateLazyInitializer().getPersistentClass();
        }
        return entity.getClass();
    }

    private static Integer identifier(AuditedEntity entity) {
        if (entity instanceof HibernateProxy proxy) {
            return (Integer) proxy.getHibernateLazyInitializer().getInternalIdentifier();
        }
        return entity.getId();
    }
}
