package ru.fitnessclub.service;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityNotFoundException;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.fitnessclub.domain.AttendanceStatus;
import ru.fitnessclub.domain.Booking;
import ru.fitnessclub.domain.Client;
import ru.fitnessclub.domain.Trainer;
import ru.fitnessclub.domain.TrainingSession;
import ru.fitnessclub.repository.BookingRepository;
import ru.fitnessclub.repository.ClientRepository;
import ru.fitnessclub.repository.MembershipRepository;
import ru.fitnessclub.repository.TrainerRepository;
import ru.fitnessclub.repository.TrainingSessionRepository;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@Service
public class FitnessClubService {
    private final ClientRepository clients;
    private final MembershipRepository memberships;
    private final TrainerRepository trainers;
    private final TrainingSessionRepository sessions;
    private final BookingRepository bookings;
    private final EntityManager entityManager;

    public FitnessClubService(
        ClientRepository clients,
        MembershipRepository memberships,
        TrainerRepository trainers,
        TrainingSessionRepository sessions,
        BookingRepository bookings,
        EntityManager entityManager
    ) {
        this.clients = clients;
        this.memberships = memberships;
        this.trainers = trainers;
        this.sessions = sessions;
        this.bookings = bookings;
        this.entityManager = entityManager;
    }

    @Transactional
    public int registerClient(String firstName, String lastName, Instant validFrom, Instant validUntil) {
        Client client = new Client(firstName, lastName);
        client.issueMembership(validFrom, validUntil);
        return clients.save(client).getId();
    }

    @Transactional
    public void removeMembership(int clientId) {
        Client client = clients.findWithMembershipById(clientId)
            .orElseThrow(() -> new EntityNotFoundException("Клиент " + clientId));
        client.removeMembership();
    }

    @Transactional
    public int registerTrainer(String firstName, String lastName) {
        return trainers.save(new Trainer(firstName, lastName)).getId();
    }

    @Transactional
    public int scheduleSession(int trainerId, String title, Instant startsAt) {
        Trainer trainer = trainers.findById(trainerId)
            .orElseThrow(() -> new EntityNotFoundException("Тренер " + trainerId));
        return sessions.save(trainer.scheduleSession(title, startsAt)).getId();
    }

    @Transactional
    public int bookSession(int clientId, int sessionId) {
        Client client = clients.getReferenceById(clientId);
        TrainingSession session = sessions.getReferenceById(sessionId);
        return bookings.save(new Booking(client, session)).getId();
    }

    @Transactional
    public BookingView markAttendance(int bookingId, AttendanceStatus status) {
        Booking booking = bookings.findById(bookingId)
            .orElseThrow(() -> new EntityNotFoundException("Запись " + bookingId));
        booking.markAttendance(status);
        entityManager.flush();
        return toBookingView(booking);
    }

    @Transactional(readOnly = true)
    public List<BookingView> findBookings(int sessionId, AttendanceStatus status) {
        return bookings.findBySessionIdAndAttendanceStatusOrderById(sessionId, status).stream()
            .map(FitnessClubService::toBookingView)
            .toList();
    }

    @Transactional(readOnly = true)
    public Optional<MembershipView> findActiveMembership(int clientId, Instant at) {
        return memberships.findActiveAt(clientId, at)
            .map(membership -> new MembershipView(
                membership.getId(), clientId, membership.getValidFrom(), membership.getValidUntil()));
    }

    @Transactional(readOnly = true)
    public TrainerSchedule findTrainerSchedule(int trainerId) {
        Trainer trainer = trainers.findWithSessions(trainerId)
            .orElseThrow(() -> new EntityNotFoundException("Тренер " + trainerId));
        List<SessionView> schedule = trainer.getSessions().stream()
            .sorted(Comparator.comparing(TrainingSession::getStartsAt).thenComparing(TrainingSession::getId))
            .map(FitnessClubService::toSessionView)
            .toList();
        return new TrainerSchedule(trainer.getId(), trainer.getFirstName(), trainer.getLastName(), schedule);
    }

    @Transactional(readOnly = true)
    public List<SessionView> searchSessions(Integer trainerId, Instant startsFrom, Instant startsBefore) {
        CriteriaBuilder builder = entityManager.getCriteriaBuilder();
        CriteriaQuery<TrainingSession> query = builder.createQuery(TrainingSession.class);
        Root<TrainingSession> session = query.from(TrainingSession.class);
        List<Predicate> filters = new ArrayList<>();
        if (trainerId != null) {
            filters.add(builder.equal(session.get("trainer").get("id"), trainerId));
        }
        if (startsFrom != null) {
            filters.add(builder.greaterThanOrEqualTo(session.<Instant>get("startsAt"), startsFrom));
        }
        if (startsBefore != null) {
            filters.add(builder.lessThan(session.<Instant>get("startsAt"), startsBefore));
        }
        query.select(session).orderBy(builder.asc(session.get("startsAt")), builder.asc(session.get("id")));
        if (!filters.isEmpty()) {
            query.where(filters.toArray(Predicate[]::new));
        }
        return entityManager.createQuery(query).getResultList().stream()
            .map(FitnessClubService::toSessionView)
            .toList();
    }

    private static SessionView toSessionView(TrainingSession session) {
        return new SessionView(session.getId(), session.getTrainer().getId(), session.getTitle(),
            session.getStartsAt());
    }

    private static BookingView toBookingView(Booking booking) {
        return new BookingView(booking.getId(), booking.getClient().getId(), booking.getSession().getId(),
            booking.getAttendanceStatus(), booking.getCreatedAt(), booking.getUpdatedAt(), booking.getVersion());
    }

    public record SessionView(int id, int trainerId, String title, Instant startsAt) {
    }

    public record BookingView(int id, int clientId, int sessionId, AttendanceStatus status,
                              Instant createdAt, Instant updatedAt, long version) {
    }

    public record MembershipView(int id, int clientId, Instant validFrom, Instant validUntil) {
    }

    public record TrainerSchedule(int id, String firstName, String lastName, List<SessionView> sessions) {
    }
}
