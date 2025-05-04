package itsc4155.jobsearch.application;

import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface ApplicationRepository extends MongoRepository<Application, String> {

    Optional<Application> findById(String id);

    List<Application> findByPostingId(String postingId);

    List<Application> findByApplicantUsername(String applicantUsername);

}