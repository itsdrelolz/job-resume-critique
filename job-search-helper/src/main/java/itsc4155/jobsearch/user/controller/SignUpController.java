package itsc4155.jobsearch.user.controller;

import itsc4155.jobsearch.session.SessionHandler;
import itsc4155.jobsearch.user.User;
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

import java.util.Base64;

@Controller
public class SignUpController {

    @Autowired
    private SessionHandler sessionHandler;

    @Autowired
    private UserService userService;

    @GetMapping("/signup")
    public Mono<String> signUp(Model model, WebSession webSession) {
        model.addAttribute("user", new User());
        sessionHandler.injectDefaultSessionData(model, webSession);

        // Redirect to profile if user is already logged in
        if (sessionHandler.isLoggedIn(webSession)) {
            return Mono.just("redirect:/profile");
        }

        return Mono.just("signup");
    }

    @PostMapping("/signup")
    public String signUpSubmit(@ModelAttribute User user, BindingResult result, Model model, WebSession session) {

        sessionHandler.injectDefaultSessionData(model, session);

        // Check if user is registering with an already existing username
        if (userService.userExists(user.getUsername())) {
            result.rejectValue("username", "error.user", "Username is already taken!");
            return "signup";
        }
        //Encrypt password
        user.setPassword(Base64.getEncoder().encodeToString(user.getPassword().getBytes()));

        userService.saveUser(user);
        return "redirect:/login";
    }
}
