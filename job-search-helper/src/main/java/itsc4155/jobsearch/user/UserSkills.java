package itsc4155.jobsearch.user;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;

@Getter
@RequiredArgsConstructor
public enum UserSkills {

    FRONT_END("Front End"),
    BACK_END("Back End"),
    JAVA("Java"),
    C("C"),
    C_SHARP("C#"),
    C_PLUS_PLUS("C++"),
    PYTHON("Python"),
    JAVASCRIPT("JavaScript");

    private final String name;

    public static UserSkills getByName(String name) {
        return Arrays.stream(UserSkills.values())
                .filter(skill -> skill.name.equalsIgnoreCase(name))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Invalid skill name: " + name));
    }
}
