package itsc4155.jobsearch.posting;

import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface PostingRepository extends MongoRepository<Posting, String> {

    Optional<Posting> findById(String id);

    List<Posting> findByAuthorUsername(String authorUsername);

}
