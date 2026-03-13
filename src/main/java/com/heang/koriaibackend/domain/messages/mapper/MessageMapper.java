package com.heang.koriaibackend.domain.messages.mapper;

import com.heang.koriaibackend.domain.messages.model.Message;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Optional;

@Mapper
public interface MessageMapper {

    int insert(Message message);

    Optional<Message> findById(@Param("id") Long id);

    List<Message> findByConversationId(@Param("conversationId") Long conversationId,
                                       @Param("limit") int limit,
                                       @Param("offset") int offset);

    int countByConversationId(@Param("conversationId") Long conversationId);

    int deleteByConversationId(@Param("conversationId") Long conversationId);
}
