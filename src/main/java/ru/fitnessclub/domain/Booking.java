package ru.fitnessclub.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.persistence.Version;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.util.Objects;

@Entity
@Table(name = "bookings", uniqueConstraints = @UniqueConstraint(name = "bookings_client_session_key", columnNames = {"client_id", "session_id"}))
public class Booking extends AuditedEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "bookings_id_seq")
    @SequenceGenerator(name = "bookings_id_seq", sequenceName = "bookings_id_seq", allocationSize = 1)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "client_id", nullable = false)
    private Client client;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "session_id", nullable = false)
    private TrainingSession session;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(nullable = false, columnDefinition = "attendance_status")
    private AttendanceStatus attendanceStatus = AttendanceStatus.PENDING;

    @Version
    @Column(nullable = false)
    private long version;

    @Override
    public Integer getId() {
        return id;
    }

    protected Booking() {
    }

    public Booking(Client client, TrainingSession session) {
        this.client = client;
        this.session = session;
    }

    public void markAttendance(AttendanceStatus status) {
        attendanceStatus = Objects.requireNonNull(status);
    }

    public Client getClient() {
        return client;
    }

    public TrainingSession getSession() {
        return session;
    }

    public AttendanceStatus getAttendanceStatus() {
        return attendanceStatus;
    }

    public long getVersion() {
        return version;
    }
}
