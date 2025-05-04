package itsc4155.jobsearch.session;

import itsc4155.jobsearch.notification.Notification;
import itsc4155.jobsearch.notification.service.NotificationService;
import itsc4155.jobsearch.user.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.ui.Model;
import org.springframework.web.server.WebSession;

import java.util.Comparator;
import java.util.List;

@Component
public class SessionHandler {

    @Autowired
    private NotificationService notificationService;

    public boolean isLoggedIn(WebSession session) {
        return session.getAttribute("loggedInUser") != null && session.getAttribute("loggedInUser") instanceof User;
    }

    public User getLoggedInUser(WebSession session) {
        return session.getAttribute("loggedInUser");
    }

    public void setLoggedInUser(WebSession session, User user) {
        session.getAttributes().put("loggedInUser", user);
    }

    public void invalidateSession(WebSession session) {
        session.invalidate();
    }

    public void injectDefaultSessionData(Model model, WebSession session) {
        boolean loggedIn = isLoggedIn(session);

        model.addAttribute("loggedIn", loggedIn);

        User loggedInUser = getLoggedInUser(session);
        if (loggedIn) {
            model.addAttribute("loggedInUser", loggedInUser);
            List<Notification> notifications = notificationService.findByUsername(loggedInUser.getUsername());
            notifications.sort(Comparator.comparing(Notification::getTimestamp).reversed());
            model.addAttribute("notifications", notifications);
            model.addAttribute("unreadCount", notifications.stream().filter(n -> !n.isRead()).count());
        }
    }
}
