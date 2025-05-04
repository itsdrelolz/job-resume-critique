// src/main/java/com/example/openai_chat_app/service/OpenAIService.java
package itsc4155.jobsearch.interview.service;

import itsc4155.jobsearch.interview.dto.ChatMessage;
import itsc4155.jobsearch.interview.dto.OpenAIRequest;
import itsc4155.jobsearch.interview.dto.OpenAIResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;

@Service
public class OpenAIService {

    @Value("${openai.api.key}")
    private String apiKey;

    @Value("${openai.api.url}")
    private String apiUrl;

    private final RestTemplate restTemplate = new RestTemplate();

    public String getChatResponse(String userMessage) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(apiKey);

        List<ChatMessage> messages = List.of(
                new ChatMessage("system",
                        "You are an intelligent and friendly technical interviewer. Also be ready if the candidate comes from a different field then interview them in their respected field. Ask the candidate interview questions one by one and respond naturally based on their answers. Start by asking the first question. If the candidate asks for a resume critique provide them."),
                new ChatMessage("user", userMessage));

        OpenAIRequest request = new OpenAIRequest("gpt-4o", messages);

        HttpEntity<OpenAIRequest> entity = new HttpEntity<>(request, headers);

        ResponseEntity<OpenAIResponse> response = restTemplate.postForEntity(
                apiUrl,
                entity,
                OpenAIResponse.class);

        return response.getBody().getChoices().get(0).getMessage().getContent();
    }

}