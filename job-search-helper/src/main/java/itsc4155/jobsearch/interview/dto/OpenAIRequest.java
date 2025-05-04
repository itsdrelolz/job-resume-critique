// src/main/java/com/example/openai_chat_app/dto/OpenAIRequest.java
package itsc4155.jobsearch.interview.dto;

import java.util.List;

public class OpenAIRequest {
    private String model;
    private List<ChatMessage> messages;

    public OpenAIRequest() {
    }

    public OpenAIRequest(String model, List<ChatMessage> messages) {
        this.model = model;
        this.messages = messages;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public List<ChatMessage> getMessages() {
        return messages;
    }

    public void setMessages(List<ChatMessage> messages) {
        this.messages = messages;
    }
}