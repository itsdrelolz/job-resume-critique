package itsc4155.jobsearch.posting.controller;

import itsc4155.jobsearch.posting.Posting;
import itsc4155.jobsearch.posting.service.PostingService;
import itsc4155.jobsearch.session.SessionHandler;
import itsc4155.jobsearch.user.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.server.WebSession;
import reactor.core.publisher.Mono;

@Controller
public class EditPostController {

    @Autowired
    private SessionHandler sessionHandler;

    @Autowired
    private PostingService postingService;

    @GetMapping("/edit-post/{id}")
    public Mono<String> editPost(@PathVariable("id") String id, Model model, WebSession webSession) {

        if (!sessionHandler.isLoggedIn(webSession)) {
            return Mono.just("redirect:/login");
        }

        Posting posting = postingService.findById(id).orElse(null);
        if (posting == null) {
            return Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND, "This posting was not found. Maybe it was deleted."));
        }

        User loggedInuser = sessionHandler.getLoggedInUser(webSession);

        //Verify user is the author
        if (!loggedInuser.getUsername().equalsIgnoreCase(posting.getAuthorUsername())) {
            return Mono.error(new ResponseStatusException(HttpStatus.FORBIDDEN, "You are not the owner of this posting."));
        }

        sessionHandler.injectDefaultSessionData(model, webSession);

        model.addAttribute("posting", posting);

        return Mono.just("edit-post");
    }

    @PostMapping("/edit-post/{id}")
    public Mono<String> updatePost(@PathVariable("id") String id, Model model, @ModelAttribute Posting posting, WebSession webSession) {
        if (!sessionHandler.isLoggedIn(webSession)) {
            return Mono.just("redirect:/login");
        }

        Posting existingPosting = postingService.findById(id).get();

        User loggedInuser = sessionHandler.getLoggedInUser(webSession);

        //Verify user is the author
        if (!loggedInuser.getUsername().equalsIgnoreCase(existingPosting.getAuthorUsername())) {
            return Mono.error(new ResponseStatusException(HttpStatus.FORBIDDEN, "You are not the owner of this posting."));
        }

        existingPosting.setJobTitle(posting.getJobTitle());
        existingPosting.setCompanyName(posting.getCompanyName());
        existingPosting.setJobDescription(posting.getJobDescription());

        postingService.save(existingPosting);

        sessionHandler.injectDefaultSessionData(model, webSession);
        return Mono.just("redirect:/post/" + id);
    }
}
