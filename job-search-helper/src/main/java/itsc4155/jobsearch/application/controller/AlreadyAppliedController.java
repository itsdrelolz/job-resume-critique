package itsc4155.jobsearch.application.controller;

import itsc4155.jobsearch.session.SessionHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.server.WebSession;
import reactor.core.publisher.Mono;

@Controller
public class AlreadyAppliedController {

    @Autowired
    private SessionHandler sessionHandler;

    @GetMapping("/already-applied")
    public Mono<String> alreadyApplied(Model model, WebSession session) {
        sessionHandler.injectDefaultSessionData(model, session);

        return Mono.just("already-applied");
    }
}
