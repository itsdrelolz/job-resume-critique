package itsc4155.jobsearch.user;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Base64;
import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
@Document(collection = "users")  // MongoDB collection name
public class User {

    @Id
    private String id;

    private String firstName;
    private String lastName;
    private String username;
    private String password;
    private int mockInterviewCredits;
    private Set<String> mockInterviewCreditsFrom = new HashSet<>();
    private Set<UserSkills> userSkills = new HashSet<>();
    private Set<String> likedPosts = new HashSet<>();

    public boolean matchPassword(String password) {
        String encodedPassword = Base64.getEncoder().encodeToString(password.getBytes());
        return encodedPassword.equals(this.password);
    }

    public void likePost(String postId) {
        likedPosts.add(postId);
    }

    public void unlikePost(String postId) {
        likedPosts.remove(postId);
    }

    public boolean isPostLiked(String postId) {
        return likedPosts.contains(postId);
    }
}