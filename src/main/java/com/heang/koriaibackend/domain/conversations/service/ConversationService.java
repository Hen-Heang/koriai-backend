package com.heang.koriaibackend.domain.conversations.service;

import com.heang.koriaibackend.domain.conversations.dto.CreateConversationRequest;
import com.heang.koriaibackend.domain.conversations.dto.UpdateConversationTitleRequest;
import com.heang.koriaibackend.domain.conversations.mapper.ConversationMapper;
import com.heang.koriaibackend.domain.conversations.model.Conversation;
import com.heang.koriaibackend.domain.users.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ConversationService {

    private final ConversationMapper conversationMapper;
    private final UserMapper userMapper;

    @Transactional
    public Optional<Conversation> create(CreateConversationRequest req) {
        if (userMapper.findById(req.userId()).isEmpty()) {
            return Optional.empty();
        }

        Conversation conversation = Conversation.builder()
                .userId(req.userId())
                .scenarioId(req.scenarioId())
                .title(req.title().trim())
                .conversationType(req.conversationType().trim().toUpperCase())
                .modelUsed(req.modelUsed().trim())
                .build();

        conversationMapper.insert(conversation);
        return conversationMapper.findById(conversation.getId());
    }

    public Optional<Conversation> findById(Long id) {
        return conversationMapper.findById(id);
    }

    public List<Conversation> findByUserId(Long userId, int limit, int offset) {
        return conversationMapper.findByUserId(userId, limit, offset);
    }

    @Transactional
    public Optional<Conversation> updateTitle(Long id, UpdateConversationTitleRequest req) {
        int updated = conversationMapper.updateTitle(id, req.title().trim());
        if (updated == 0) {
            return Optional.empty();
        }
        return conversationMapper.findById(id);
    }

    @Transactional
    public boolean incrementMessageCount(Long id) {
        return conversationMapper.incrementMessageCount(id) > 0;
    }

    @Transactional
    public boolean deleteById(Long id) {
        return conversationMapper.deleteById(id) > 0;
    }
}
