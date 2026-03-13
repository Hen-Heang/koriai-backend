package com.heang.koriaibackend.domain.messages.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.heang.koriaibackend.domain.messages.model.Message;
import com.heang.koriaibackend.domain.messages.service.MessageService;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(MessageController.class)
@AutoConfigureMockMvc(addFilters = false)
class MessageControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @MockitoBean
    private MessageService messageService;

    @MockitoBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    private Message sampleMessage() {
        return Message.builder()
                .id(1L)
                .conversationId(10L)
                .role("USER")
                .content("안녕하세요")
                .tokensUsed(10)
                .createdAt(OffsetDateTime.now())
                .build();
    }

    @Test
    void create_ShouldReturnMessage() throws Exception {
        when(messageService.create(any())).thenReturn(Optional.of(sampleMessage()));

        mockMvc.perform(post("/api/messages")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "conversationId", 10,
                                "role", "USER",
                                "content", "안녕하세요",
                                "tokensUsed", 10
                        ))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status.code").value(200))
                .andExpect(jsonPath("$.data.role").value("USER"))
                .andExpect(jsonPath("$.data.content").value("안녕하세요"));
    }

    @Test
    void create_WhenConversationNotFound_ShouldReturn404() throws Exception {
        when(messageService.create(any())).thenReturn(Optional.empty());

        mockMvc.perform(post("/api/messages")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "conversationId", 999,
                                "role", "USER",
                                "content", "hello",
                                "tokensUsed", 5
                        ))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status.code").value(404));
    }

    @Test
    void findById_ShouldReturnMessage() throws Exception {
        when(messageService.findById(1L)).thenReturn(Optional.of(sampleMessage()));

        mockMvc.perform(get("/api/messages/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status.code").value(200))
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.conversationId").value(10));
    }

    @Test
    void findById_WhenNotFound_ShouldReturn404() throws Exception {
        when(messageService.findById(anyLong())).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/messages/999"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status.code").value(404));
    }

    @Test
    void findByConversationId_ShouldReturnList() throws Exception {
        when(messageService.findByConversationId(eq(10L), anyInt(), anyInt()))
                .thenReturn(List.of(sampleMessage()));

        mockMvc.perform(get("/api/messages/conversation/10")
                        .param("limit", "50")
                        .param("offset", "0"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status.code").value(200))
                .andExpect(jsonPath("$.data[0].conversationId").value(10))
                .andExpect(jsonPath("$.data[0].role").value("USER"));
    }

    @Test
    void countByConversationId_ShouldReturnCount() throws Exception {
        when(messageService.countByConversationId(10L)).thenReturn(5);

        mockMvc.perform(get("/api/messages/conversation/10/count"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status.code").value(200))
                .andExpect(jsonPath("$.data.count").value(5));
    }
}