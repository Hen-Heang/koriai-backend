package com.heang.koriaibackend.domain.diary.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.heang.koriaibackend.domain.diary.dto.DiaryEntryResponse;
import com.heang.koriaibackend.domain.diary.service.DiaryService;
import com.heang.koriaibackend.security.jwt.JwtAuthenticationFilter;
import com.heang.koriaibackend.security.model.AuthUser;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(DiaryController.class)
@AutoConfigureMockMvc(addFilters = false)
class DiaryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @MockitoBean
    private DiaryService diaryService;

    @MockitoBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @BeforeEach
    void setAuthContext() {
        AuthUser authUser = new AuthUser(1L, "user@test.com");
        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(authUser, null, Collections.emptyList());
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }

    @AfterEach
    void clearAuthContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void createDiary_ShouldReturnSavedEntry() throws Exception {
        DiaryEntryResponse response = new DiaryEntryResponse(
                1L,
                LocalDate.of(2026, 3, 13),
                "오늘은 기분이 좋다",
                "오늘은 기분이 좋아요.",
                "Polite ending improved",
                4,
                "happy",
                OffsetDateTime.now()
        );
        when(diaryService.createOrUpdate(eq(1L), eq(LocalDate.of(2026, 3, 13)), eq("오늘은 기분이 좋다"))).thenReturn(response);

        mockMvc.perform(post("/api/diary")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "entryDate", "2026-03-13",
                                "originalText", "오늘은 기분이 좋다"
                        ))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status.code").value(200))
                .andExpect(jsonPath("$.data.mood").value("happy"));
    }

    @Test
    void getDiaryByMonth_ShouldReturnEntries() throws Exception {
        DiaryEntryResponse response = new DiaryEntryResponse(
                1L,
                LocalDate.of(2026, 3, 13),
                "오늘은 기분이 좋다",
                "오늘은 기분이 좋아요.",
                "Nice diary",
                4,
                "happy",
                OffsetDateTime.now()
        );
        when(diaryService.getByMonth(1L, "2026-03")).thenReturn(List.of(response));

        mockMvc.perform(get("/api/diary").param("month", "2026-03"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status.code").value(200))
                .andExpect(jsonPath("$.data[0].entryDate").value("2026-03-13"));
    }
}
