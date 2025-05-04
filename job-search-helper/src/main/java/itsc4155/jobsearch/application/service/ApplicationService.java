package itsc4155.jobsearch.application.service;

import itsc4155.jobsearch.application.Application;
import itsc4155.jobsearch.application.ApplicationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ApplicationService {

    @Autowired
    private ApplicationRepository applicationRepository;

   public Optional<Application> findById(String id) {
        return applicationRepository.findById(id);
    }

    public List<Application> findByPostingId(String postingId) {
       return applicationRepository.findByPostingId(postingId);
    }

    public List<Application> findByApplicantUsername(String applicantUsername) {
        return applicationRepository.findByApplicantUsername(applicantUsername);
    }

    public Optional<Application> findApplicationByUsernameAndPostingId(String applicantUsername, String postingId) {
        return applicationRepository.findByApplicantUsername(applicantUsername).stream().filter(application -> application.getPostingId().equals(postingId)).findFirst();
    }

    public void save(Application application) {
        applicationRepository.save(application);
    }

}
