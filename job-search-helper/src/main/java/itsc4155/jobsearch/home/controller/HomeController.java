package itsc4155.jobsearch.home.controller;

import itsc4155.jobsearch.session.SessionHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.server.WebSession;

@Controller
public class HomeController {

    @Autowired
    public SessionHandler sessionHandler;

    @GetMapping("/")
    public String backslash(Model model, WebSession session) {
        return homeController(model, session);
    }

    @GetMapping("/home")
    public String home(Model model, WebSession session) {
        return homeController(model, session);
    }

    public String homeController(Model model, WebSession session) {
        model.addAttribute("pageTitle", "Home - SelfBuilder");
        sessionHandler.injectDefaultSessionData(model, session);
        return "home";
    }
}
