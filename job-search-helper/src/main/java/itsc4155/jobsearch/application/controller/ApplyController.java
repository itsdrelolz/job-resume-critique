package itsc4155.jobsearch.application.controller;

import itsc4155.jobsearch.application.Application;
import itsc4155.jobsearch.application.service.ApplicationService;
import itsc4155.jobsearch.notification.Notification;
import itsc4155.jobsearch.notification.service.NotificationService;
import itsc4155.jobsearch.posting.Posting;
import itsc4155.jobsearch.posting.service.PostingService;
import itsc4155.jobsearch.session.SessionHandler;
import itsc4155.jobsearch.tika.TikaHandler;
import itsc4155.jobsearch.user.User;
import org.apache.tika.Tika;
import org.apache.tika.exception.TikaException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.server.WebSession;
import reactor.core.publisher.Mono;

import java.io.ByteArrayInputStream;
import java.io.IOException;

@Controller
public class ApplyController {

    @Autowired
    private SessionHandler sessionHandler;

    @Autowired
    private PostingService postingService;

    @Autowired
    private ApplicationService applicationService;

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private TikaHandler tikaHandler;

    @GetMapping("/apply/{id}")
    public Mono<String> apply(@PathVariable("id") String id, Model model, WebSession webSession) {
        if (!sessionHandler.isLoggedIn(webSession)) {
            return Mono.just("redirect:/login");
        }
        sessionHandler.injectDefaultSessionData(model, webSession);

        Posting posting = postingService.findById(id).orElse(null);

        //Redirect user to error page if posting is not found
        if (posting == null) {
            return Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND, "This posting was not found. Maybe it was deleted."));
        }

        User user = sessionHandler.getLoggedInUser(webSession);

        Application existingApplication = applicationService.findApplicationByUsernameAndPostingId(user.getUsername(), id).orElse(null);
        if (existingApplication != null) {
            return Mono.just("redirect:/already-applied");
        }

        //Verify user isn't the author
        if (user.getUsername().equalsIgnoreCase(posting.getAuthorUsername())) {
            return Mono.error(new ResponseStatusException(HttpStatus.FORBIDDEN, "You are not allowed to apply to your own posting."));
        }
        model.addAttribute("posting", posting);
        return Mono.just("apply");
    }

    @PostMapping("/apply")
    public Mono<String> apply(@RequestPart(name = "postingId") String postingId,
                              @RequestPart(name = "applicantName") String applicantName,
                              @RequestPart(name = "email") String email,
                              @RequestPart(name = "resumeFile") Mono<FilePart> filePartMono,
                              Model model,
                              WebSession webSession) {

        Tika tika = tikaHandler.getTika();

        return filePartMono
                .flatMap(filePart ->
                        DataBufferUtils.join(filePart.content()) // Join the data buffers into one
                                .map(dataBuffer -> {
                                    byte[] bytes = new byte[dataBuffer.readableByteCount()];
                                    dataBuffer.read(bytes);
                                    DataBufferUtils.release(dataBuffer); // release buffer
                                    return bytes;
                                })
                                .flatMap(bytes -> {
                                    try {
                                        Posting posting = postingService.findById(postingId).orElse(null);
                                        if (posting == null) {
                                            return Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND, "This posting was not found. Maybe it was deleted."));
                                        }

                                        String extractedText = tika.parseToString(new ByteArrayInputStream(bytes));

                                        User user = sessionHandler.getLoggedInUser(webSession);

                                        Application application = new Application();
                                        application.setPostingId(postingId);
                                        application.setApplicantUsername(user.getUsername());
                                        application.setApplicantName(applicantName);
                                        application.setApplicantEmail(email);
                                        application.setResumeText(extractedText);
                                        application.setStatus("Pending");
                                        application.setTimestamp(System.currentTimeMillis());

                                        applicationService.save(application);

                                        Notification notification = new Notification();
                                        notification.setUsername(posting.getAuthorUsername());
                                        notification.setMessage("[" + posting.getJobTitle() + "] " + applicantName + " (" + user.getUsername() + ") has just applied to your job posting!");
                                        notification.setStatusUpdate(false);
                                        notification.setTimestamp(System.currentTimeMillis());

                                        notificationService.save(notification);
                                    } catch (IOException | TikaException e) {
                                        e.printStackTrace();
                                    }
                                    return Mono.just("redirect:/applied");
                                })
                );
    }
}
