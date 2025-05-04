package itsc4155.jobsearch.notification;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Getter
@Setter
@Document(collection = "notifications")
public class Notification {

    @Id
    private String id;

    private String username;
    private String message;
    private String postingId;
    private boolean statusUpdate;
    private boolean read;
    private long timestamp;
}
