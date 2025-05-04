package itsc4155.jobsearch.user.controller;

import itsc4155.jobsearch.session.SessionHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.server.WebSession;
import reactor.core.publisher.Mono;

@Controller
public class LogoutController {

    @Autowired
    private SessionHandler sessionHandler;

    @GetMapping("/logout")
    public Mono<String> logout(Model model, WebSession webSession) {

        //Invalidate user session
        sessionHandler.invalidateSession(webSession);

        //Send user to login page after logging out
        return Mono.just("redirect:/login");
    }
}
