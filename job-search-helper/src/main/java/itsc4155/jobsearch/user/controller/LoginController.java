package itsc4155.jobsearch.user.controller;

import itsc4155.jobsearch.session.SessionHandler;
import itsc4155.jobsearch.user.User;
import itsc4155.jobsearch.user.UserRepository;
import itsc4155.jobsearch.user.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.server.WebSession;
import reactor.core.publisher.Mono;

@Controller
public class LoginController {

    @Autowired
    private UserService userService;

    @Autowired
    private SessionHandler sessionHandler;

    @GetMapping("/login")
    public Mono<String> login(Model model, WebSession webSession) {
        model.addAttribute("user", new User());

        sessionHandler.injectDefaultSessionData(model, webSession);

        // Redirect to profile if user is already logged in
        if (sessionHandler.isLoggedIn(webSession)) {
            return Mono.just("redirect:/profile");
        }

        return Mono.just("login");
    }

    @PostMapping("/login")
    public Mono<String> loginSubmit(@ModelAttribute User user, BindingResult result, Model model, WebSession webSession) {

        sessionHandler.injectDefaultSessionData(model, webSession);

        //User account doesn't exist
        if (!userService.userExists(user.getUsername())) {
            result.rejectValue("username", "error.user.not.found", "That user was not found.");
            return Mono.just("login");
        }
        // Fetch user from the repository by username
        User foundUser = userService.findByUsernameIgnoreCase(user.getUsername()).orElse(null);

        //Verify password
        if (!foundUser.matchPassword(user.getPassword())) {
            result.rejectValue("password", "error.incorrect.password", "The password you entered is incorrect.");
            return Mono.just("login");
        }
        // Save user session
        sessionHandler.setLoggedInUser(webSession, foundUser);
        return Mono.just("redirect:/profile");
    }
}
