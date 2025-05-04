package itsc4155.jobsearch.posting.controller;

import itsc4155.jobsearch.posting.Posting;
import itsc4155.jobsearch.posting.service.PostingService;
import itsc4155.jobsearch.session.SessionHandler;
import itsc4155.jobsearch.user.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.server.WebSession;
import reactor.core.publisher.Mono;

@Controller
public class DeletePostController {

    @Autowired
    private SessionHandler sessionHandler;

    @Autowired
    private PostingService postingService;

    @PostMapping("/delete-post/{id}")
    public Mono<String> deletePost(Model model, @PathVariable("id") String id, WebSession webSession) {

        sessionHandler.injectDefaultSessionData(model, webSession);

        if (!sessionHandler.isLoggedIn(webSession)) {
            return Mono.just("redirect:/login");
        }

        Posting posting = postingService.findById(id).get();

        //Verify post exists
        if (posting == null) {
            return Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND, "This posting was not found. Maybe it was deleted."));
        }

        User loggedInuser = sessionHandler.getLoggedInUser(webSession);

        //Verify user is the author
        if (!loggedInuser.getUsername().equalsIgnoreCase(posting.getAuthorUsername())) {
            return Mono.error(new ResponseStatusException(HttpStatus.FORBIDDEN, "You are not the owner of this posting."));
        }
        postingService.delete(posting);
        return Mono.just("redirect:/posts");
    }

}
