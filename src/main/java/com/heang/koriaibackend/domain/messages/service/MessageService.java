package com.heang.koriaibackend.domain.messages.service;

import com.heang.koriaibackend.domain.conversations.mapper.ConversationMapper;
import com.heang.koriaibackend.domain.messages.dto.CreateMessageRequest;
import com.heang.koriaibackend.domain.messages.mapper.MessageMapper;
import com.heang.koriaibackend.domain.messages.model.Message;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class MessageService {

    private final MessageMapper messageMapper;
    private final ConversationMapper conversationMapper;

    @Transactional
    public Optional<Message> create(CreateMessageRequest req) {
        if (conversationMapper.findById(req.conversationId()).isEmpty()) {
            return Optional.empty();
        }

        Message message = Message.builder()
                .conversationId(req.conversationId())
                .role(req.role().trim().toUpperCase())
                .content(req.content().trim())
                .corrections(req.corrections())
                .tokensUsed(req.tokensUsed())
                .build();
        messageMapper.insert(message);
        conversationMapper.incrementMessageCount(req.conversationId());
        return messageMapper.findById(message.getId());
    }

    public Optional<Message> findById(Long id) {
        return messageMapper.findById(id);
    }

    public List<Message> findByConversationId(Long conversationId, int limit, int offset) {
        return messageMapper.findByConversationId(conversationId, limit, offset);
    }

    public int countByConversationId(Long conversationId) {
        return messageMapper.countByConversationId(conversationId);
    }
}
