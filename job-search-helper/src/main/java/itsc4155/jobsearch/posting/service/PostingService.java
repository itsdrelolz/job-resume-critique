package itsc4155.jobsearch.posting.service;

import itsc4155.jobsearch.posting.Posting;
import itsc4155.jobsearch.posting.PostingRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class PostingService {

    @Autowired
    private PostingRepository postingRepository;

    public Optional<Posting> findById(String id) {
        return postingRepository.findById(id);
    }

    public List<Posting> findAll() {
        return postingRepository.findAll();
    }

    public List<Posting> findByAuthorUsername(String authorsUsername) {
        return postingRepository.findAll().stream().filter(posting -> posting.getAuthorUsername().equalsIgnoreCase(authorsUsername)).collect(Collectors.toList());
    }

    public Posting save(Posting posting) {
        return postingRepository.save(posting);
    }

    public void delete(Posting posting) {
        postingRepository.delete(posting);
    }
}