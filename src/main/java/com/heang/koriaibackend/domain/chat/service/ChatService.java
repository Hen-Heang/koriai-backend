package com.heang.koriaibackend.domain.chat.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.heang.koriaibackend.ai.OpenAiService;
import com.heang.koriaibackend.ai.dto.OpenAiResult;
import com.heang.koriaibackend.common.api.Code;
import com.heang.koriaibackend.common.exception.BusinessException;
import com.heang.koriaibackend.common.util.PromptTemplates;
import com.heang.koriaibackend.domain.chat.dto.ChatSendResponse;
import com.heang.koriaibackend.domain.chat.dto.CreateChatConversationRequest;
import com.heang.koriaibackend.domain.conversations.mapper.ConversationMapper;
import com.heang.koriaibackend.domain.conversations.model.Conversation;
import com.heang.koriaibackend.domain.messages.mapper.MessageMapper;
import com.heang.koriaibackend.domain.messages.model.Message;
import com.heang.koriaibackend.domain.usage.service.ApiUsageLogService;
import com.heang.koriaibackend.domain.users.mapper.UserMapper;
import com.heang.koriaibackend.domain.users.model.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ChatService {

    private final ConversationMapper conversationMapper;
    private final MessageMapper messageMapper;
    private final UserMapper userMapper;
    private final OpenAiService openAiService;
    private final ApiUsageLogService apiUsageLogService;
    private final ObjectMapper objectMapper;

    @Transactional
    public Conversation createConversation(Long userId, CreateChatConversationRequest req) {
        User user = userMapper.findById(userId).orElseThrow(() -> new BusinessException(Code.NOT_FOUND, "User not found"));
        Conversation conversation = Conversation.builder()
                .userId(userId)
                .scenarioId(req.scenarioId())
                .title(req.title().trim())
                .conversationType(req.conversationType().trim().toUpperCase())
                .modelUsed(user.getPreferredModel())
                .messageCount(0)
                .build();
        conversationMapper.insert(conversation);
        return conversationMapper.findById(conversation.getId()).orElseThrow(() -> new BusinessException(Code.SYSTEM_ERROR));
    }

    public List<Conversation> getConversations(Long userId, int limit, int offset) {
        return conversationMapper.findByUserId(userId, limit, offset);
    }

    public List<Message> getConversationMessages(Long userId, Long conversationId, int limit, int offset) {
        Conversation conversation = requireOwnedConversation(userId, conversationId);
        return messageMapper.findByConversationId(conversation.getId(), limit, offset);
    }

    @Transactional
    public ChatSendResponse sendMessage(Long userId, String text, Long conversationId) {
        Conversation conversation = requireOwnedConversation(userId, conversationId);
        User user = userMapper.findById(userId).orElseThrow(() -> new BusinessException(Code.NOT_FOUND, "User not found"));

        Message userMessage = Message.builder()
                .conversationId(conversationId)
                .role("USER")
                .content(text.trim())
                .tokensUsed(0)
                .build();
        messageMapper.insert(userMessage);
        conversationMapper.incrementMessageCount(conversationId);

        String prompt = PromptTemplates.chatPrompt(text, conversation.getConversationType(), user.getKoreanLevel());
        OpenAiResult result = openAiService.generate(prompt, conversation.getModelUsed());

        Message assistantMessage = Message.builder()
                .conversationId(conversationId)
                .role("ASSISTANT")
                .content(result.content())
                .tokensUsed(result.completionTokens())
                .build();
        messageMapper.insert(assistantMessage);
        conversationMapper.incrementMessageCount(conversationId);

        apiUsageLogService.log(userId, "CHAT", result);
        return new ChatSendResponse(conversationId, userMessage.getId(), assistantMessage.getId(), assistantMessage.getContent());
    }

    public SseEmitter streamMessage(Long userId, String text, Long conversationId) {
        Conversation conversation = requireOwnedConversation(userId, conversationId);
        User user = userMapper.findById(userId).orElseThrow(() -> new BusinessException(Code.NOT_FOUND, "User not found"));

        Message userMessage = Message.builder()
                .conversationId(conversationId)
                .role("USER")
                .content(text.trim())
                .tokensUsed(0)
                .build();
        messageMapper.insert(userMessage);
        conversationMapper.incrementMessageCount(conversationId);

        String prompt = PromptTemplates.chatPrompt(text, conversation.getConversationType(), user.getKoreanLevel());
        String modelUsed = conversation.getModelUsed();

        SseEmitter emitter = new SseEmitter(120_000L);
        
        // Use a background thread for processing
        java.util.concurrent.CompletableFuture.runAsync(() -> {
            StringBuilder fullResponse = new StringBuilder();
            try {
                emitter.send(SseEmitter.event()
                        .name("start")
                        .data(objectMapper.writeValueAsString(Map.of("userMessageId", userMessage.getId()))));

                openAiService.generateStream(prompt, modelUsed, token -> {
                    fullResponse.append(token);
                    try {
                        emitter.send(SseEmitter.event()
                                .name("token")
                                .data(objectMapper.writeValueAsString(Map.of("token", token))));
                    } catch (IOException e) {
                        // Client disconnected — we can stop here
                        throw new RuntimeException("Client disconnected", e);
                    }
                });

                Message assistantMessage = Message.builder()
                        .conversationId(conversationId)
                        .role("ASSISTANT")
                        .content(fullResponse.toString())
                        .tokensUsed(0)
                        .build();
                messageMapper.insert(assistantMessage);
                conversationMapper.incrementMessageCount(conversationId);

                emitter.send(SseEmitter.event()
                        .name("done")
                        .data(objectMapper.writeValueAsString(Map.of("assistantMessageId", assistantMessage.getId()))));
                emitter.complete();
            } catch (Exception e) {
                if (!e.getMessage().contains("Client disconnected")) {
                    try {
                        emitter.send(SseEmitter.event()
                                .name("error")
                                .data(objectMapper.writeValueAsString(Map.of("message", "Streaming failed"))));
                    } catch (Exception ignored) {}
                }
                emitter.completeWithError(e);
            }
        });

        return emitter;
    }

    private Conversation requireOwnedConversation(Long userId, Long conversationId) {
        Conversation conversation = conversationMapper.findById(conversationId)
                .orElseThrow(() -> new BusinessException(Code.NOT_FOUND, "Conversation not found"));
        if (!conversation.getUserId().equals(userId)) {
            throw new BusinessException(Code.INSUFFICIENT_PERMISSIONS);
        }
        return conversation;
    }
}
