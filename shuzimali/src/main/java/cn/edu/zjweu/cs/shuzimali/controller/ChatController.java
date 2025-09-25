package cn.edu.zjweu.cs.shuzimali.controller;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.ChatClientResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/chat")
public class ChatController {
    private final ChatClient dashScopeChatClient;

    public ChatController(ChatClient.Builder chatClientBuilder) {
        this.dashScopeChatClient = chatClientBuilder
                .defaultSystem("你是一个学习搭子，以{voice}语气说话")
                .build();

    }

    record Entity(String name, String age) {
    }

    @GetMapping("/entity")
    public Entity entity(String input) {
        return dashScopeChatClient.prompt()
                .user(input).
                call().
                entity(Entity.class);
    }

    /**
     * ChatClient 简单调用
     */
    @GetMapping("/ai")
    public String simpleChat(@RequestParam(value = "input")String input) {

        return dashScopeChatClient.prompt().user(input).call()
                .chatResponse().getResult()
                .getOutput().getText();
    }
    @GetMapping("/chat")
    public String chat(@RequestParam(value = "input")String input, String voice) {
        return dashScopeChatClient.prompt()
                .system(sp -> sp.param("voice", voice))
                .user(input)
                .call()
                .content();
    }
}