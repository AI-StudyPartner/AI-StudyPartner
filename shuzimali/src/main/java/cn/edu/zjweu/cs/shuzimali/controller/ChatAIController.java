package cn.edu.zjweu.cs.shuzimali.controller;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

@RestController
public class ChatAIController {
    private final ChatClient chatClient;

    public ChatAIController(ChatClient chatClient) {
        this.chatClient = chatClient;
    }
    
    @GetMapping("/chat")
    public String chat(String query) {
        return chatClient.prompt()
                .user(query)
                .call()
                .content();
    }
    
    @GetMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE + ";charset=UTF-8")
    public Flux<String> stream(String query) {
        return chatClient.prompt()
                .user(query)
                .stream()
                .content();
    }
}