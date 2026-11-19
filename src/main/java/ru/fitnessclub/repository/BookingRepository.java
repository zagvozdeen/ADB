package ru.fitnessclub.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.fitnessclub.domain.AttendanceStatus;
import ru.fitnessclub.domain.Booking;

import java.util.List;

public interface BookingRepository extends JpaRepository<Booking, Integer> {
    List<Booking> findBySessionIdAndAttendanceStatusOrderById(int sessionId, AttendanceStatus attendanceStatus);
}
