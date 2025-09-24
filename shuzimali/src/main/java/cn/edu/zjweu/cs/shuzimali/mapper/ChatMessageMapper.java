package cn.edu.zjweu.cs.shuzimali.mapper;

import cn.edu.zjweu.cs.shuzimali.entity.ChatMessage;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface ChatMessageMapper {
    List<ChatMessage> listByConversation(@Param("conversationId") long conversationId, @Param("userId") int userId);
    void insert(ChatMessage message);
}


