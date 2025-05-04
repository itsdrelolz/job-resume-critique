package itsc4155.jobsearch.notification;

import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface NotificationRepository extends MongoRepository<Notification, String> {

    Optional<Notification> findById(String id);

    List<Notification> findByUsername(String username);
}
