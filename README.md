# Фитнес-клуб

- **Название:** Фитнес-клуб, тема № 8
- **Студент:** Загвоздин Денис Сергеевич
- **Группа:** РИМ-250950

## Запуск

Понадобятся JDK 17+ и Docker с Compose. Maven Wrapper скачает Maven при первом запуске.

1. `docker compose up -d`
2. `./mvnw package`
3. `docker compose exec -T postgres pg_isready -h 127.0.0.1 -U fitness -d fitness`. Дождись ответа `accepting connections`.
4. `java -jar target/fitness-club-0.0.1-SNAPSHOT.jar --spring.profiles.active=demo`

| Сервис | Адрес | Имя базы | Пользователь | Пароль |
| --- | --- | --- | --- | --- |
| PostgreSQL 18 | `127.0.0.1:5432` | `fitness` | `fitness` | `fitness` |

Профиль `demo` создаёт двух клиентов, тренера, два занятия и три записи. Сценарий выводит результаты запросов, отмечает посещение и проверяет удаление абонемента. Каждый запуск добавляет новый набор демонстрационных данных.

Том `adb-data` смонтирован в `/var/lib/postgresql`. Команда `docker compose down` останавливает PostgreSQL и сохраняет именованный том.

## Схема базы данных

| Связь | Таблицы | Ограничение |
| --- | --- | --- |
| Тренер → занятия, 1:N | `trainers`, `training_sessions` | Каждое занятие ссылается на одного тренера через `trainer_id`. |
| Клиент ↔ занятие, N:M | `clients`, `bookings`, `training_sessions` | Пара `(client_id, session_id)` уникальна. Дата записи хранится в `created_at`, статус — в `attendance_status` (`PENDING`, `ATTENDED`, `ABSENT`). |
| Клиент → абонемент, 1:1 | `clients`, `memberships` | `client_id` уникален. Абонемент действует в интервале `[valid_from, valid_until)`. |

Схему создаёт Flyway. Первичные ключи объявлены как `SERIAL PRIMARY KEY`, внешние ключи имеют тип `INTEGER`. [Миграция V1](src/main/resources/db/migration/V1__create_fitness_club_schema.sql)

## Самопроверка по заданию

| Пункт | Класс или метод | Что проверить |
| --- | --- | --- |
| Двусторонняя связь 1:N | `Trainer.sessions`, `TrainingSession.trainer`, `Trainer.scheduleSession` | Занятие привязано к тренеру и входит в его расписание. |
| Связь N:M | `Booking.client`, `Booking.session` | У одного клиента есть записи на разные занятия, а у одного занятия есть записи разных клиентов. |
| Связь 1:1, каскад и удаление | `Client.membership`, `Membership.client`, `FitnessClubService.removeMembership` | Абонемент сохраняется вместе с клиентом через `CascadeType.PERSIST` и удаляется через `orphanRemoval`. |
| Запрос из имени метода | `BookingRepository.findBySessionIdAndAttendanceStatusOrderById` | Записи на занятие фильтруются по статусу. |
| Native SQL | `MembershipRepository.findActiveAt` | Находит действующий абонемент, включая момент `valid_from`. В момент `valid_until` абонемент уже истёк. |
| JPQL | `TrainerRepository.findWithSessions` | Расписание тренера загружается через `join fetch`. |
| Criteria API | `FitnessClubService.searchSessions` | Фильтры по тренеру и границам времени применяются, если параметры заданы. |
| Транзакции и DTO | `FitnessClubService` | Транзакции открывает сервис. Методы чтения используют `readOnly = true` и возвращают DTO. |
| Генерация ID | `@GeneratedValue` и `@SequenceGenerator` в каждой сущности | Hibernate использует стратегию `SEQUENCE` и последовательность, которую PostgreSQL создаёт для `SERIAL`. |
| Метки времени и версия | `AuditedEntity`, `Booking.version`, `FitnessClubService.markAttendance` | При смене статуса обновляется `updated_at`, версия увеличивается. Сущности сравниваются по типу и ID. |
| Демонстрация | `DemoRunner` | Профиль `demo` запускает проверки и выводит результаты в консоль. |

У каждой из пяти сущностей есть свой `JpaRepository`. Для JPA-связей явно задан `LAZY`, настройка `open-in-view` отключена.
