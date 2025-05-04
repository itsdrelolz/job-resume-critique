package itsc4155.jobsearch.notification.service;

import itsc4155.jobsearch.notification.Notification;
import itsc4155.jobsearch.notification.NotificationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class NotificationService {

    @Autowired
    private NotificationRepository notificationRepository;

    public Optional<Notification> findById(String id) {
        return notificationRepository.findById(id);
    }

    public List<Notification> findByUsername(String username) {
        return notificationRepository.findByUsername(username);
    }

    public void markAllAsRead(String username) {
        findByUsername(username).stream().forEach(notification -> {
            notification.setRead(true);
            notificationRepository.save(notification);
        });
    }

    public void save(Notification notification) {
        notificationRepository.save(notification);
    }

    public void delete(Notification notification) {
        notificationRepository.delete(notification);
    }
}
