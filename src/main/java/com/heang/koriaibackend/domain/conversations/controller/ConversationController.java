package com.heang.koriaibackend.domain.conversations.controller;

import com.heang.koriaibackend.common.api.ApiResponse;
import com.heang.koriaibackend.common.api.Code;
import com.heang.koriaibackend.domain.conversations.dto.ConversationResponse;
import com.heang.koriaibackend.domain.conversations.dto.CreateConversationRequest;
import com.heang.koriaibackend.domain.conversations.dto.UpdateConversationTitleRequest;
import com.heang.koriaibackend.domain.conversations.service.ConversationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/conversations")
@RequiredArgsConstructor
@Validated
public class ConversationController {

    private final ConversationService conversationService;

    @PostMapping
    public ApiResponse<?> create(@Valid @RequestBody CreateConversationRequest req) {
        return conversationService.create(req)
                .<ApiResponse<?>>map(conversation -> ApiResponse.success(ConversationResponse.from(conversation)))
                .orElseGet(() -> ApiResponse.error(Code.NOT_FOUND, Map.of("message", "User not found")));
    }

    @GetMapping("/{id}")
    public ApiResponse<?> findById(@PathVariable Long id) {
        return conversationService.findById(id)
                .<ApiResponse<?>>map(conversation -> ApiResponse.success(ConversationResponse.from(conversation)))
                .orElseGet(() -> ApiResponse.error(Code.NOT_FOUND, Map.of("message", "Conversation not found")));
    }

    @GetMapping
    public ApiResponse<List<ConversationResponse>> findByUserId(@RequestParam Long userId,
                                                                @RequestParam(defaultValue = "20") int limit,
                                                                @RequestParam(defaultValue = "0") int offset) {
        List<ConversationResponse> data = conversationService.findByUserId(userId, limit, offset)
                .stream()
                .map(ConversationResponse::from)
                .toList();
        return ApiResponse.success(data);
    }

    @PutMapping("/{id}/title")
    public ApiResponse<?> updateTitle(@PathVariable Long id, @Valid @RequestBody UpdateConversationTitleRequest req) {
        return conversationService.updateTitle(id, req)
                .<ApiResponse<?>>map(conversation -> ApiResponse.success(ConversationResponse.from(conversation)))
                .orElseGet(() -> ApiResponse.error(Code.NOT_FOUND, Map.of("message", "Conversation not found")));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Map<String, Boolean>> delete(@PathVariable Long id) {
        boolean deleted = conversationService.deleteById(id);
        if (!deleted) {
            return ApiResponse.error(Code.NOT_FOUND, Map.of("deleted", false));
        }
        return ApiResponse.success(Map.of("deleted", true));
    }
}
