package com.heang.koriaibackend.domain.conversations.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.heang.koriaibackend.domain.conversations.model.Conversation;
import com.heang.koriaibackend.domain.conversations.service.ConversationService;
import com.heang.koriaibackend.security.jwt.JwtAuthenticationFilter;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ConversationController.class)
@AutoConfigureMockMvc(addFilters = false)
class ConversationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @MockitoBean
    private ConversationService conversationService;

    @MockitoBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    private Conversation sampleConversation() {
        return Conversation.builder()
                .id(1L)
                .userId(10L)
                .title("Grammar Practice")
                .conversationType("GRAMMAR_PRACTICE")
                .modelUsed("gpt-5-mini")
                .messageCount(0)
                .createdAt(OffsetDateTime.now())
                .updatedAt(OffsetDateTime.now())
                .build();
    }

    @Test
    void create_ShouldReturnConversation() throws Exception {
        when(conversationService.create(any())).thenReturn(Optional.of(sampleConversation()));

        mockMvc.perform(post("/api/conversations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "userId", 10,
                                "title", "Grammar Practice",
                                "conversationType", "GRAMMAR_PRACTICE",
                                "modelUsed", "gpt-5-mini"
                        ))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status.code").value(200))
                .andExpect(jsonPath("$.data.title").value("Grammar Practice"))
                .andExpect(jsonPath("$.data.conversationType").value("GRAMMAR_PRACTICE"));
    }

    @Test
    void create_WhenUserNotFound_ShouldReturnNotFound() throws Exception {
        when(conversationService.create(any())).thenReturn(Optional.empty());

        mockMvc.perform(post("/api/conversations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "userId", 999,
                                "title", "Test",
                                "conversationType", "FREE_CHAT",
                                "modelUsed", "gpt-5-mini"
                        ))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status.code").value(404));
    }

    @Test
    void findById_ShouldReturnConversation() throws Exception {
        when(conversationService.findById(1L)).thenReturn(Optional.of(sampleConversation()));

        mockMvc.perform(get("/api/conversations/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status.code").value(200))
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.modelUsed").value("gpt-5-mini"));
    }

    @Test
    void findById_WhenNotFound_ShouldReturn404() throws Exception {
        when(conversationService.findById(anyLong())).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/conversations/999"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status.code").value(404));
    }

    @Test
    void findByUserId_ShouldReturnList() throws Exception {
        when(conversationService.findByUserId(eq(10L), anyInt(), anyInt()))
                .thenReturn(List.of(sampleConversation()));

        mockMvc.perform(get("/api/conversations")
                        .param("userId", "10")
                        .param("limit", "20")
                        .param("offset", "0"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status.code").value(200))
                .andExpect(jsonPath("$.data[0].userId").value(10));
    }

    @Test
    void updateTitle_ShouldReturnUpdatedConversation() throws Exception {
        Conversation updated = sampleConversation();
        updated.setTitle("Updated Title");
        when(conversationService.updateTitle(eq(1L), any())).thenReturn(Optional.of(updated));

        mockMvc.perform(put("/api/conversations/1/title")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("title", "Updated Title"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status.code").value(200))
                .andExpect(jsonPath("$.data.title").value("Updated Title"));
    }

    @Test
    void delete_ShouldReturnDeleted() throws Exception {
        when(conversationService.deleteById(1L)).thenReturn(true);

        mockMvc.perform(delete("/api/conversations/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status.code").value(200))
                .andExpect(jsonPath("$.data.deleted").value(true));
    }

    @Test
    void delete_WhenNotFound_ShouldReturn404() throws Exception {
        when(conversationService.deleteById(anyLong())).thenReturn(false);

        mockMvc.perform(delete("/api/conversations/999"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status.code").value(404));
    }
}
