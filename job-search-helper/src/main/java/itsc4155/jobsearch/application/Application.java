package itsc4155.jobsearch.application;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Getter
@Setter
@Document(collection = "applications")
public class Application {

    @Id
    private String id;

    private String applicantUsername;
    private String applicantName;
    private String applicantEmail;
    private String postingId;
    private String resumeText;
    private String status;
    private long timestamp;
}
