package cn.edu.zjweu.cs.shuzimali.controller;

import cn.edu.zjweu.cs.shuzimali.Factory.UserFactory;
import cn.edu.zjweu.cs.shuzimali.entity.ChatMessage;
import cn.edu.zjweu.cs.shuzimali.entity.Conversation;
import cn.edu.zjweu.cs.shuzimali.mapper.ChatMessageMapper;
import cn.edu.zjweu.cs.shuzimali.mapper.ConversationMapper;
import cn.edu.zjweu.cs.shuzimali.pojo.Result;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/conversation")
public class ConversationController {

    @Resource
    private ConversationMapper conversationMapper;

    @Resource
    private ChatMessageMapper chatMessageMapper;

    @GetMapping("/list")
    public Result list() {
        int userId = Integer.parseInt(UserFactory.getUser().getId());
        List<Conversation> list = conversationMapper.listByUserId(userId);
        return Result.success(list);
    }

    @PostMapping("/create")
    public Result create(@RequestBody Conversation conversation) {
        int userId = Integer.parseInt(UserFactory.getUser().getId());
        conversation.setUserId(userId);
        conversationMapper.insert(conversation);
        return Result.success(conversation.getId());
    }

    @GetMapping("/{conversationId}/messages")
    public Result messages(@PathVariable("conversationId") long conversationId) {
        int userId = Integer.parseInt(UserFactory.getUser().getId());
        List<ChatMessage> msgs = chatMessageMapper.listByConversation(conversationId, userId);
        return Result.success(msgs);
    }

    @PostMapping("/{conversationId}/message")
    public Result appendMessage(@PathVariable("conversationId") long conversationId, @RequestBody ChatMessage message) {
        int userId = Integer.parseInt(UserFactory.getUser().getId());
        message.setConversationId(conversationId);
        message.setUserId(userId);
        chatMessageMapper.insert(message);
        if ("assistant".equals(message.getRole()) || "user".equals(message.getRole())) {
            conversationMapper.updateLastMessage(conversationId, message.getContent());
        }
        Map<String, Object> resp = new HashMap<>();
        resp.put("messageId", message.getId());
        return Result.success(resp);
    }
}


