package itsc4155.jobsearch.notification.controller;

import itsc4155.jobsearch.notification.service.NotificationService;
import itsc4155.jobsearch.session.SessionHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.server.WebSession;
import reactor.core.publisher.Mono;

@Controller
public class NotificationController {

    @Autowired
    private SessionHandler sessionHandler;

    @Autowired
    private NotificationService notificationService;

    @PostMapping("/notifications/mark-read")
    public Mono<String> markAllRead(WebSession session,
                                    @org.springframework.web.bind.annotation.RequestHeader(value = "Referer", required = false) String referer) {

        if (!sessionHandler.isLoggedIn(session)) {
            return Mono.just("redirect:/login");
        }

        notificationService.markAllAsRead(sessionHandler.getLoggedInUser(session).getUsername());

        // Go back to previous page, or home if referer is missing
        return Mono.just("redirect:" + (referer != null ? referer : "/home"));
    }

}
