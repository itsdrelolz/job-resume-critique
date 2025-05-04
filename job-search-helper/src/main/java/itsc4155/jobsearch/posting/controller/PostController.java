package itsc4155.jobsearch.posting.controller;

import itsc4155.jobsearch.posting.Posting;
import itsc4155.jobsearch.posting.PostingRepository;
import itsc4155.jobsearch.posting.service.FetchPostingsService;
import itsc4155.jobsearch.posting.service.PostingService;
import itsc4155.jobsearch.session.SessionHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.server.WebSession;
import reactor.core.publisher.Mono;

@Controller
public class PostController {

    @Autowired
    private SessionHandler sessionHandler;

    @Autowired
    private PostingService postingService;

    @Autowired
    private FetchPostingsService fetchPostingsService;

    @GetMapping("/post/{id}")
    public Mono<String> post(Model model, @PathVariable("id") String id, WebSession webSession) {
        sessionHandler.injectDefaultSessionData(model, webSession);

        Posting posting = null;
        if (id.startsWith("thirdparty")) {
            posting = fetchPostingsService.findById(id);
        } else {
            posting = postingService.findById(id).orElse(null);
        }

        //Redirect user to error page if posting is not found
        if (posting == null) {
            return Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND, "This posting was not found. Maybe it was deleted."));
        }

        // External redirect
        if (posting.isPulledViaApi()) {
            String externalUrl = posting.getUrl();
            if (!externalUrl.startsWith("http")) {
                externalUrl = "https://" + externalUrl; // ensure it's valid
            }
            return Mono.just("redirect:" + externalUrl);
        }

        model.addAttribute("posting", posting);

        return Mono.just("post");
    }
}
