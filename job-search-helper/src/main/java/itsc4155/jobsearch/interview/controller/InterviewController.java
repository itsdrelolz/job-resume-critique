package itsc4155.jobsearch.interview.controller;

import itsc4155.jobsearch.interview.service.OpenAIService;
import itsc4155.jobsearch.session.SessionHandler;
import itsc4155.jobsearch.tika.TikaHandler;
import itsc4155.jobsearch.user.User;
import itsc4155.jobsearch.user.service.UserService;
import org.apache.tika.exception.TikaException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.server.WebSession;
import reactor.core.publisher.Mono;

import java.io.ByteArrayInputStream;
import java.io.IOException;

@Controller
public class InterviewController {

    @Autowired
    private SessionHandler sessionHandler;

    @Autowired
    private OpenAIService openAIService;

    @Autowired
    private TikaHandler tikaHandler;

    @Autowired
    private UserService userService;

    @GetMapping("/interview")
    public Mono<String> showChatPage(Model model, WebSession session) {
        sessionHandler.injectDefaultSessionData(model, session);
        model.addAttribute("pageTitle", "Mock Interview - SelfBuilder");

        if (!sessionHandler.isLoggedIn(session)) {
            return Mono.just("redirect:/login");
        }

        User loggedInUser = sessionHandler.getLoggedInUser(session);
        model.addAttribute("credits", loggedInUser.getMockInterviewCredits());

        return Mono.just("chat");
    }

    @PostMapping("/api/resume/upload")
    public Mono<ResponseEntity<String>> uploadResume(@RequestPart("file") Mono<FilePart> filePartMono, Model model, WebSession webSession) {

        sessionHandler.injectDefaultSessionData(model, webSession);

        return filePartMono.flatMap(filePart -> DataBufferUtils.join(filePart.content())
                .flatMap(dataBuffer -> {
                    byte[] bytes = new byte[dataBuffer.readableByteCount()];
                    dataBuffer.read(bytes);
                    DataBufferUtils.release(dataBuffer);

                    try {
                        // Extract resume text using Apache Tika
                        String resumeText = tikaHandler.getTika().parseToString(new ByteArrayInputStream(bytes));

                        String prompt = "You're a technical interviewer. Before starting the interview, please remind the user not to refresh the page because they will lose their interview credit and they will not be able to resume the interview. Based on the following resume, ask the candidate an interview question:\n\n"
                                + resumeText;

                        if (!sessionHandler.isLoggedIn(webSession)) {
                            return Mono.just(ResponseEntity.ok("You are not logged in."));
                        }

                        User loggedInuser = sessionHandler.getLoggedInUser(webSession);

                        if (loggedInuser.getMockInterviewCredits() <= 0) {
                            return Mono.just(ResponseEntity.ok("You don't have a mock interview credit!"));
                        }

                        //Subtract mock interview credit
                        loggedInuser.setMockInterviewCredits(loggedInuser.getMockInterviewCredits() - 1);
                        userService.saveUser(loggedInuser);

                        String gptResponse = openAIService.getChatResponse(prompt);
                        return Mono.just(ResponseEntity.ok(gptResponse));

                    } catch (IOException | TikaException e) {
                        e.printStackTrace();
                        return Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                .body("Error processing resume: " + e.getMessage()));
                    }
                }));
    }

    @PostMapping("/api/chat")
    public Mono<ResponseEntity<String>> chat(@RequestBody ChatRequest request, WebSession session) {
        String prompt = "You're a technical interviewer. Respond to the candidate's message:\n\n" + request.getMessage();
        String reply = openAIService.getChatResponse(prompt);
        return Mono.just(ResponseEntity.ok(reply));
    }

    public static class ChatRequest {
        private String message;
        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }
    }
}