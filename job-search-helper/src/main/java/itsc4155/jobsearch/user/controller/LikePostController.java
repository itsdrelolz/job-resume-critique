package itsc4155.jobsearch.user.controller;

import itsc4155.jobsearch.session.SessionHandler;
import itsc4155.jobsearch.user.User;
import itsc4155.jobsearch.user.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.server.WebSession;
import reactor.core.publisher.Mono;

@Controller
public class LikePostController {

    @Autowired
    private SessionHandler sessionHandler;

    @Autowired
    private UserService userService;

    @PostMapping("/likePost/{id}")
    public Mono<String> likePost(@PathVariable("id") String postId,
                                 WebSession webSession,
                                 @RequestHeader(value = "Referer", required = false) String referer) {
        if (!sessionHandler.isLoggedIn(webSession)) {
            return Mono.just("redirect:/login");
        }

        User user = sessionHandler.getLoggedInUser(webSession);

        // Toggle like/unlike
        if (user.isPostLiked(postId)) {
            user.unlikePost(postId);
        } else {
            user.likePost(postId);
        }

        userService.saveUser(user);  // Save the updated user

        // Redirect back to the page they came from
        return Mono.just("redirect:" + (referer != null ? referer : "/posts"));
    }

}
