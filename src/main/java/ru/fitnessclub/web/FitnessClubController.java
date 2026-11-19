package ru.fitnessclub.web;

import jakarta.persistence.EntityNotFoundException;
import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.fitnessclub.domain.AttendanceStatus;
import ru.fitnessclub.service.FitnessClubService;
import ru.fitnessclub.service.FitnessClubService.BookingView;
import ru.fitnessclub.service.FitnessClubService.MembershipView;
import ru.fitnessclub.service.FitnessClubService.SessionView;
import ru.fitnessclub.service.FitnessClubService.TrainerSchedule;

import java.time.Instant;
import java.util.List;

@RestController
@Profile("http")
@RequestMapping("/api")
public class FitnessClubController {
    private final FitnessClubService service;

    public FitnessClubController(FitnessClubService service) {
        this.service = service;
    }

    @GetMapping("/sessions")
    public List<SessionView> findSessions(
        @RequestParam(required = false) Integer trainerId,
        @RequestParam(required = false) Instant startsFrom,
        @RequestParam(required = false) Instant startsBefore
    ) {
        return service.searchSessions(trainerId, startsFrom, startsBefore);
    }

    @GetMapping("/trainers/{trainerId}/schedule")
    public ResponseEntity<TrainerSchedule> findSchedule(@PathVariable int trainerId) {
        try {
            return ResponseEntity.ok(service.findTrainerSchedule(trainerId));
        } catch (EntityNotFoundException exception) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/sessions/{sessionId}/bookings")
    public List<BookingView> findBookings(@PathVariable int sessionId, @RequestParam AttendanceStatus status) {
        return service.findBookings(sessionId, status);
    }

    @GetMapping("/clients/{clientId}/active-membership")
    public ResponseEntity<MembershipView> findActiveMembership(@PathVariable int clientId, @RequestParam Instant at) {
        return ResponseEntity.of(service.findActiveMembership(clientId, at));
    }
}
