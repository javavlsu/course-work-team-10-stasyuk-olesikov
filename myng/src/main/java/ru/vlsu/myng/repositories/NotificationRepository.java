package ru.vlsu.myng.repositories;

import ru.vlsu.myng.entities.Notification;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Репозиторий системных уведомлений.
 */
public interface NotificationRepository extends JpaRepository<Notification, Integer>
{
}