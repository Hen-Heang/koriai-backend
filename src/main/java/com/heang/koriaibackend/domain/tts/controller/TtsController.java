package com.heang.koriaibackend.domain.tts.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@RestController
@RequestMapping("/api/tts")
public class TtsController {

    @Value("${openai.api-key}")
    private String apiKey;

    private final RestTemplate restTemplate = new RestTemplate();

    public record TtsRequest(String text, String voice) {}

    @PostMapping(produces = "audio/mpeg")
    public ResponseEntity<byte[]> speak(@RequestBody TtsRequest req) {
        String voice = (req.voice() != null && !req.voice().isBlank()) ? req.voice() : "nova";
        String text = req.text() != null ? req.text().trim() : "";
        if (text.isBlank()) {
            return ResponseEntity.badRequest().build();
        }
        // Limit text length to control cost
        if (text.length() > 500) {
            text = text.substring(0, 500);
        }

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + apiKey);
        headers.setContentType(MediaType.APPLICATION_JSON);

        Map<String, String> body = Map.of(
                "model", "tts-1",
                "input", text,
                "voice", voice
        );

        HttpEntity<Map<String, String>> entity = new HttpEntity<>(body, headers);
        ResponseEntity<byte[]> response = restTemplate.exchange(
                "https://api.openai.com/v1/audio/speech",
                HttpMethod.POST,
                entity,
                byte[].class
        );

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType("audio/mpeg"))
                .body(response.getBody());
    }
}