package itsc4155.jobsearch.posting.controller;

import itsc4155.jobsearch.posting.Posting;
import itsc4155.jobsearch.posting.PostingRepository;
import itsc4155.jobsearch.posting.service.PostingService;
import itsc4155.jobsearch.session.SessionHandler;
import itsc4155.jobsearch.user.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.server.WebSession;
import reactor.core.publisher.Mono;

@Controller
public class CreatePostController {

    @Autowired
    private SessionHandler sessionHandler;

    @Autowired
    private PostingService postingService;

    @GetMapping("/create-post")
    public Mono<String> createPost(Model model, WebSession webSession) {
        if (!sessionHandler.isLoggedIn(webSession)) {
            return Mono.just("redirect:/login");
        }
        sessionHandler.injectDefaultSessionData(model, webSession);
        return Mono.just("create-post");
    }

    @PostMapping("/submit-post")
    public Mono<String> submitPost(Model model, @ModelAttribute Posting posting, WebSession webSession) {

        if (!sessionHandler.isLoggedIn(webSession)) {
            return Mono.just("redirect:/login");
        }

        User user = sessionHandler.getLoggedInUser(webSession);

        sessionHandler.injectDefaultSessionData(model, webSession);

        //Save timestamp and author
        posting.setTimestamp(System.currentTimeMillis());
        posting.setAuthorUsername(user.getUsername());

        postingService.save(posting);
        return Mono.just("redirect:/posts?selected=" + posting.getId());
    }
}
