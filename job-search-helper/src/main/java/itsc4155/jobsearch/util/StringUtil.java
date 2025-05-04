package itsc4155.jobsearch.util;

public class StringUtil {

    public static String trimToFirstWords(String input, int wordCount) {
        String[] words = input.trim().split("\\s+");

        if (words.length <= wordCount) {
            return input;
        }

        StringBuilder result = new StringBuilder();
        for (int i = 0; i < wordCount; i++) {
            result.append(words[i]).append(" ");
        }

        return result.toString().trim();
    }
}
