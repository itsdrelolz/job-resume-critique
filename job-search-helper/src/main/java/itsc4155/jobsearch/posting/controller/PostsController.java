package itsc4155.jobsearch.posting.controller;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.server.WebSession;

import itsc4155.jobsearch.posting.Posting;
import itsc4155.jobsearch.posting.service.FetchPostingsService;
import itsc4155.jobsearch.posting.service.PostingService;
import itsc4155.jobsearch.session.SessionHandler;
import reactor.core.publisher.Mono;

@Controller
public class PostsController {

    ArrayList<Posting> fetchedPosts;

    @Autowired
    private SessionHandler sessionHandler;

    @Autowired
    private PostingService postingService;

    @Autowired
    private FetchPostingsService fetchPostingsService;

    @GetMapping("/posts")
    public Mono<String> posts(
            @RequestParam(value = "page", required = false, defaultValue = "0") int page,
            @RequestParam(value = "size", required = false, defaultValue = "5") int size,
            @RequestParam(value = "selected", required = false) String selected,
            Model model, WebSession webSession) {

        List<Posting> allPostings = postingService.findAll();
        allPostings.addAll(fetchPostingsService.getCachedPostings()); // Use cached postings

        int total = allPostings.size();
        int fromIndex = Math.min(page * size, total);
        int toIndex = Math.min(fromIndex + size, total);
        List<Posting> postingsPage = allPostings.subList(fromIndex, toIndex);
        model.addAttribute("postingsPage", postingsPage);
        model.addAttribute("currentPage", page);
        model.addAttribute("pageSize", size);
        model.addAttribute("totalPostings", total);

        Posting selectedPosting = null;
        if (selected != null && !selected.isEmpty()) {
            // Check user-created posts first
            selectedPosting = postingService.findById(selected).orElseGet(() ->
                    // Check API-pulled posts if not found in the database
                    fetchPostingsService.getCachedPostings().stream()
                            .filter(posting -> posting.getId().equals(selected))
                            .findFirst()
                            .orElse(null)
            );
        }

        if (selectedPosting == null && !postingsPage.isEmpty()) {
            selectedPosting = postingsPage.get(0); // Default to the first post if no valid selection
        }

        if (selectedPosting == null) {
            model.addAttribute("errorMessage", "Invalid or missing selection.");
            model.addAttribute("errorDescription", "The selected job posting could not be found.");
            return Mono.just("error");
        }

        model.addAttribute("selectedPosting", selectedPosting);
        sessionHandler.injectDefaultSessionData(model, webSession);

        return Mono.just("posts");
    }
}