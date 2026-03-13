package com.heang.koriaibackend.domain.messages.controller;

import com.heang.koriaibackend.common.api.ApiResponse;
import com.heang.koriaibackend.common.api.Code;
import com.heang.koriaibackend.domain.messages.dto.CreateMessageRequest;
import com.heang.koriaibackend.domain.messages.dto.MessageResponse;
import com.heang.koriaibackend.domain.messages.service.MessageService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/messages")
@RequiredArgsConstructor
@Validated
public class MessageController {

    private final MessageService messageService;

    @PostMapping
    public ApiResponse<?> create(@Valid @RequestBody CreateMessageRequest req) {
        return messageService.create(req)
                .<ApiResponse<?>>map(message -> ApiResponse.success(MessageResponse.from(message)))
                .orElseGet(() -> ApiResponse.error(Code.NOT_FOUND, Map.of("message", "Conversation not found")));
    }

    @GetMapping("/{id}")
    public ApiResponse<?> findById(@PathVariable Long id) {
        return messageService.findById(id)
                .<ApiResponse<?>>map(message -> ApiResponse.success(MessageResponse.from(message)))
                .orElseGet(() -> ApiResponse.error(Code.NOT_FOUND, Map.of("message", "Message not found")));
    }

    @GetMapping("/conversation/{conversationId}")
    public ApiResponse<List<MessageResponse>> findByConversationId(@PathVariable Long conversationId,
                                                                   @RequestParam(defaultValue = "50") int limit,
                                                                   @RequestParam(defaultValue = "0") int offset) {
        List<MessageResponse> data = messageService.findByConversationId(conversationId, limit, offset)
                .stream()
                .map(MessageResponse::from)
                .toList();
        return ApiResponse.success(data);
    }

    @GetMapping("/conversation/{conversationId}/count")
    public ApiResponse<Map<String, Integer>> countByConversationId(@PathVariable Long conversationId) {
        int total = messageService.countByConversationId(conversationId);
        return ApiResponse.success(Map.of("count", total));
    }
}
