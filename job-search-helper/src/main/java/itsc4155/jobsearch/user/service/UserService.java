package itsc4155.jobsearch.user.service;

import itsc4155.jobsearch.user.User;
import itsc4155.jobsearch.user.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    public Optional<User> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    public Optional<User>findByUsernameIgnoreCase(String username) {
        return userRepository.findAll().stream().filter(user -> user.getUsername().equalsIgnoreCase(username)).findFirst();
    }

    public boolean userExists(String username) {
        return findByUsernameIgnoreCase(username).isPresent();
    }

    public User saveUser(User user) {
        return userRepository.save(user);
    }
}
