package itsc4155.jobsearch.user.controller;

import itsc4155.jobsearch.application.Application;
import itsc4155.jobsearch.application.service.ApplicationService;
import itsc4155.jobsearch.posting.Posting;
import itsc4155.jobsearch.posting.service.PostingService;
import itsc4155.jobsearch.session.SessionHandler;
import itsc4155.jobsearch.user.User;
import itsc4155.jobsearch.user.UserSkills;
import itsc4155.jobsearch.user.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebSession;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

@Controller
public class ProfileController {

    @Autowired
    private SessionHandler sessionHandler;

    @Autowired
    private PostingService postingService;

    @Autowired
    private UserService userService;

    @Autowired private ApplicationService applicationService;

    @GetMapping("/profile")
    public Mono<String> profile(Model model, WebSession webSession) {
        sessionHandler.injectDefaultSessionData(model, webSession);

        // Redirect user to login page if they are not logged in
        if (!sessionHandler.isLoggedIn(webSession)) {
            return Mono.just("redirect:/login");
        }

        // Inject user's profile into the page
        User user = sessionHandler.getLoggedInUser(webSession);
        model.addAttribute("user", user);
        model.addAttribute("availableSkills", Arrays.asList(UserSkills.values()));

        List<Posting> likedPostings = new ArrayList<>();
        user.getLikedPosts().stream().filter(postingId -> postingService.findById(postingId).isPresent()).forEach(likedPostingId -> likedPostings.add(postingService.findById(likedPostingId).get()));
        model.addAttribute("likedPosts", likedPostings);

        // Inject user's postings into the page
        List<Posting> userPostings = postingService.findByAuthorUsername(user.getUsername());
        model.addAttribute("userPostings", userPostings);

        List<Application> rawApplications = applicationService.findByApplicantUsername(user.getUsername());
        List<ApplicationWithPosting> applications = rawApplications.stream()
                .map(app -> {
                    Posting post = postingService.findById(app.getPostingId()).orElse(null);
                    return new ApplicationWithPosting(app, post);
                })
                .filter(wrapper -> wrapper.getPosting() != null)
                .toList();

        model.addAttribute("applications", applications);

        return Mono.just("profile");
    }

    @PostMapping("/updateSkills")
    public Mono<String> updateSkills(ServerWebExchange exchange, Model model, WebSession webSession) {
        sessionHandler.injectDefaultSessionData(model, webSession);

        if (!sessionHandler.isLoggedIn(webSession)) {
            return Mono.just("redirect:/login");
        }

        return exchange.getFormData().map(formData -> {
            List<String> selectedSkills = formData.get("selectedSkills");
            User user = sessionHandler.getLoggedInUser(webSession);

            user.setUserSkills(selectedSkills != null ?
                    selectedSkills.stream()
                            .map(UserSkills::getByName)
                            .collect(Collectors.toSet())
                    : new HashSet<>());

            userService.saveUser(user);
            return "redirect:/profile";
        });
    }

    public class ApplicationWithPosting {
        private Application application;
        private Posting posting;

        // constructor
        public ApplicationWithPosting(Application application, Posting posting) {
            this.application = application;
            this.posting = posting;
        }

        // getters
        public Application getApplication() {
            return application;
        }

        public Posting getPosting() {
            return posting;
        }
    }

}
