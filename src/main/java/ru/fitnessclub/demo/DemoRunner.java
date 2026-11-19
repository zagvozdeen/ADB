package ru.fitnessclub.demo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import ru.fitnessclub.domain.AttendanceStatus;
import ru.fitnessclub.service.FitnessClubService;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

@Component
@Profile("demo")
public class DemoRunner implements ApplicationRunner {
    private static final Logger log = LoggerFactory.getLogger(DemoRunner.class);

    private final FitnessClubService service;

    public DemoRunner(FitnessClubService service) {
        this.service = service;
    }

    @Override
    public void run(ApplicationArguments args) {
        Instant now = Instant.now().truncatedTo(ChronoUnit.SECONDS);
        Instant expiresAt = now.plus(30, ChronoUnit.DAYS);
        int trainerId = service.registerTrainer("Ирина", "Волкова");
        int firstClientId = service.registerClient("Анна", "Соколова", now, expiresAt);
        int secondClientId = service.registerClient("Максим", "Орлов", now, expiresAt);
        Instant yogaStartsAt = now.plus(1, ChronoUnit.DAYS);
        int yogaId = service.scheduleSession(trainerId, "Йога", yogaStartsAt);
        int stretchingId = service.scheduleSession(trainerId, "Растяжка", now.plus(2, ChronoUnit.DAYS));

        int bookingId = service.bookSession(firstClientId, yogaId);
        service.bookSession(firstClientId, stretchingId);
        service.bookSession(secondClientId, yogaId);

        var schedule = service.findTrainerSchedule(trainerId);
        require(schedule.sessions().size() == 2, "Ожидали два занятия тренера");
        log.info("JPQL, расписание тренера: {}", schedule);

        var pending = service.findBookings(yogaId, AttendanceStatus.PENDING);
        require(pending.size() == 2, "Ожидали двух клиентов на йоге");
        require(service.findBookings(stretchingId, AttendanceStatus.PENDING).get(0).clientId() == firstClientId,
            "Ожидали запись первого клиента на оба занятия");
        log.info("Запрос из имени метода, записи на йогу: {}", pending);

        var membership = service.findActiveMembership(firstClientId, now).orElseThrow();
        require(service.findActiveMembership(firstClientId, expiresAt).isEmpty(),
            "Абонемент должен истечь ровно в valid_until");
        log.info("Native SQL, действующий абонемент: {}", membership);

        var filtered = service.searchSessions(trainerId, yogaStartsAt, yogaStartsAt.plus(1, ChronoUnit.HOURS));
        require(filtered.size() == 1 && filtered.get(0).id() == yogaId, "Фильтры должны выбрать только йогу");
        require(service.searchSessions(trainerId, null, null).size() == 2, "Фильтр по тренеру должен выбрать оба занятия");
        require(service.searchSessions(null, null, null).size() >= 2, "Поиск без фильтров должен найти занятия");
        log.info("Criteria API, занятия в заданном интервале: {}", filtered);

        var before = pending.stream().filter(booking -> booking.id() == bookingId).findFirst().orElseThrow();
        var updated = service.markAttendance(bookingId, AttendanceStatus.ATTENDED);
        require(service.findBookings(yogaId, AttendanceStatus.ATTENDED).size() == 1,
            "Изменение статуса должно сохраниться в PostgreSQL");
        require(updated.version() == before.version() + 1, "Версия записи должна увеличиться");
        require(updated.createdAt().equals(before.createdAt()) && updated.updatedAt().isAfter(before.updatedAt()),
            "При обновлении должна меняться только метка updated_at");
        log.info("Отметка о посещении: {}", updated);

        service.removeMembership(secondClientId);
        require(service.findActiveMembership(secondClientId, now).isEmpty(), "Абонемент должен удалиться");
        log.info("Сценарий завершён, все проверки прошли");
    }

    private static void require(boolean condition, String message) {
        if (!condition) {
            throw new IllegalStateException(message);
        }
    }
}
