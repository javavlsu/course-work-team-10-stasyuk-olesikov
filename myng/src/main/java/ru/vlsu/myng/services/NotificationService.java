package ru.vlsu.myng.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.vlsu.myng.entities.Notification;
import ru.vlsu.myng.entities.User;
import ru.vlsu.myng.repositories.NotificationRepository;
import ru.vlsu.myng.repositories.UserRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;

    public List<Notification> getByUser(Integer userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        return notificationRepository.findByUsers(user);
    }

    public List<Notification> getByType(Notification.Type type) {
        return notificationRepository.findByType(type);
    }

    public Notification save(Notification notification) {
        return notificationRepository.save(notification);
    }

    public void delete(Integer id) {
        notificationRepository.deleteById(id);
    }
}