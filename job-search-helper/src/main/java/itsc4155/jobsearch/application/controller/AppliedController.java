package itsc4155.jobsearch.application.controller;

import itsc4155.jobsearch.session.SessionHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.server.WebSession;
import reactor.core.publisher.Mono;

@Controller
public class AppliedController {

    @Autowired
    private SessionHandler sessionHandler;

    @GetMapping("/applied")
    public Mono<String> applied(Model model, WebSession webSession) {
        sessionHandler.injectDefaultSessionData(model, webSession);

        return Mono.just("applied");
    }
}
