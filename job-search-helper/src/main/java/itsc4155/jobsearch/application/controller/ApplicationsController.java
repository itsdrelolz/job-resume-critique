package itsc4155.jobsearch.application.controller;

import itsc4155.jobsearch.application.Application;
import itsc4155.jobsearch.application.service.ApplicationService;
import itsc4155.jobsearch.notification.Notification;
import itsc4155.jobsearch.notification.service.NotificationService;
import itsc4155.jobsearch.posting.Posting;
import itsc4155.jobsearch.posting.service.PostingService;
import itsc4155.jobsearch.session.SessionHandler;
import itsc4155.jobsearch.user.User;
import itsc4155.jobsearch.user.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebSession;
import reactor.core.publisher.Mono;

import java.util.List;

@Controller
public class ApplicationsController {

    @Autowired
    private SessionHandler sessionHandler;

    @Autowired
    private ApplicationService applicationService;

    @Autowired
    private PostingService postingService;

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private UserService userService;

    @GetMapping("/applications")
    public Mono<String> viewApplications(
            @RequestParam(name = "postingId") String postingId,
            @RequestParam(name = "selected", required = false) String selectedAppId,
            Model model, WebSession webSession) {

        // Inject session data
        sessionHandler.injectDefaultSessionData(model, webSession);

        if (!sessionHandler.isLoggedIn(webSession)) {
            return Mono.just("redirect:/login");
        }

        Posting posting = postingService.findById(postingId).orElse(null);
        if (posting == null) {
            return Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND, "This posting was not found. Maybe it was deleted."));
        }

        User loggedInUser = sessionHandler.getLoggedInUser(webSession);
        if (!posting.getAuthorUsername().equalsIgnoreCase(loggedInUser.getUsername())) {
            return Mono.error(new ResponseStatusException(HttpStatus.FORBIDDEN, "You are not the owner of this posting."));
        }

        // Get all applications for the posting
        List<Application> applications = applicationService.findByPostingId(postingId);
        model.addAttribute("applications", applications);

        // Get selected application
        Application selectedApp = null;
        if (selectedAppId != null) {
            selectedApp = applicationService.findById(selectedAppId).orElse(null);
        } else if (!applications.isEmpty()) {
            selectedApp = applications.get(0); // default to first application
        }
        model.addAttribute("selectedApplication", selectedApp);

        // Also load the posting info
        model.addAttribute("selectedPosting", posting);

        return Mono.just("applications");
    }

    @PostMapping("/applications/{id}/status")
    public Mono<String> updateStatus(@PathVariable("id") String id, ServerWebExchange exchange, Model model, WebSession webSession) {

        sessionHandler.injectDefaultSessionData(model, webSession);

        if (!sessionHandler.isLoggedIn(webSession)) {
            return Mono.just("redirect:/login");
        }

        return exchange.getFormData().flatMap(formData -> {
            String status = formData.getFirst("status");
            String postingId = formData.getFirst("postingId");

            Posting posting = postingService.findById(postingId).orElse(null);
            if (posting == null) {
                return Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND, "This posting was not found. Maybe it was deleted."));
            }

            User loggedInUser = sessionHandler.getLoggedInUser(webSession);
            if (!posting.getAuthorUsername().equalsIgnoreCase(loggedInUser.getUsername())) {
                return Mono.error(new ResponseStatusException(HttpStatus.FORBIDDEN, "You are not the owner of this posting."));
            }

            return Mono.fromRunnable(() -> {
                Application app = applicationService.findById(id).orElse(null);
                if (app != null && status != null) {
                    app.setStatus(status);
                    applicationService.save(app);

                    Notification notification = new Notification();
                    notification.setUsername(app.getApplicantUsername());
                    notification.setMessage("[" + posting.getJobTitle() + "] Application status changed to " + status);
                    notification.setPostingId(postingId);
                    notification.setStatusUpdate(true);
                    notification.setTimestamp(System.currentTimeMillis());

                    //Delete all previous status updates for this job posting
                    notificationService.findByUsername(app.getApplicantUsername()).stream().filter(notif -> notif.isStatusUpdate() && notif.getPostingId().equalsIgnoreCase(postingId)).forEach(notif -> {
                        notificationService.delete(notif);
                    });
                    notificationService.save(notification);

                    User applicantUser = userService.findByUsername(app.getApplicantUsername()).orElse(null);
                    //Give user mock interview credits
                    if (status.equalsIgnoreCase("Interview")) {
                        if (applicantUser != null && !applicantUser.getMockInterviewCreditsFrom().contains(postingId)) {
                            applicantUser.getMockInterviewCreditsFrom().add(postingId);

                            applicantUser.setMockInterviewCredits(applicantUser.getMockInterviewCredits() + 1);
                            userService.saveUser(applicantUser);
                        }
                    } else if (status.equalsIgnoreCase("Denied") && applicantUser.getMockInterviewCreditsFrom().contains(postingId) && applicantUser.getMockInterviewCredits() > 0) {
                        applicantUser.setMockInterviewCredits(applicantUser.getMockInterviewCredits() - 1);
                        userService.saveUser(applicantUser);
                    }
                }
            }).thenReturn("redirect:/applications?postingId=" + postingId + "&selected=" + id);
        });
    }

}
