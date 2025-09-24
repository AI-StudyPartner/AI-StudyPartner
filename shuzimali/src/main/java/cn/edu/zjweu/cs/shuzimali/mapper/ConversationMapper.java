package cn.edu.zjweu.cs.shuzimali.mapper;

import cn.edu.zjweu.cs.shuzimali.entity.Conversation;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface ConversationMapper {
    List<Conversation> listByUserId(@Param("userId") int userId);
    void insert(Conversation conversation);
    void updateLastMessage(@Param("id") long id, @Param("lastMessage") String lastMessage);
}


