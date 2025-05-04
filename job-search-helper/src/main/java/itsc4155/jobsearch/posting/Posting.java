package itsc4155.jobsearch.posting;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Document(collection = "postings")
public class Posting {

    @Id
    private String id;

    private String jobTitle;
    private String companyName;
    private String jobDescription;
    private String authorUsername;
    private long timestamp;
    private String postedDate;
    private String url;
    private boolean isPulledViaApi = false;

}