package com.heang.koriaibackend.ai;

import com.heang.koriaibackend.ai.dto.OpenAiResult;
import com.openai.client.OpenAIClient;
import com.openai.core.http.StreamResponse;
import com.openai.models.chat.completions.ChatCompletionChunk;
import com.openai.models.chat.completions.ChatCompletionCreateParams;
import com.openai.models.responses.Response;
import com.openai.models.responses.ResponseCreateParams;
import com.openai.models.responses.ResponseOutputItem;
import com.openai.models.responses.ResponseUsage;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.function.Consumer;

@Service
@RequiredArgsConstructor
public class OpenAiService {

    private final OpenAIClient client;

    @Value("${openai.model:gpt-4o-mini}")
    private String defaultModel;

    @Value("${openai.mock-enabled:false}")
    private boolean mockEnabled;

    public OpenAiResult generate(String prompt, String model) {
        String selectedModel = (model == null || model.isBlank()) ? defaultModel : model;
        if (mockEnabled) {
            int promptTokens = Math.max(1, prompt.length() / 4);
            int completionTokens = 80;
            return new OpenAiResult(
                    "Mock response: " + prompt.substring(0, Math.min(120, prompt.length())),
                    selectedModel,
                    promptTokens,
                    completionTokens,
                    30
            );
        }

        long start = System.currentTimeMillis();
        ResponseCreateParams params = ResponseCreateParams.builder()
                .model(selectedModel)
                .input(prompt)
                .build();
        Response response = client.responses().create(params);
        long elapsed = System.currentTimeMillis() - start;

        StringBuilder text = new StringBuilder();
        for (ResponseOutputItem item : response.output()) {
            if (!item.isMessage()) {
                continue;
            }

            item.asMessage().content().forEach(content -> {
                if (content.isOutputText()) {
                    text.append(content.asOutputText().text());
                }
            });
        }

        int promptTokens = 0;
        int completionTokens = 0;
        if (response.usage().isPresent()) {
            ResponseUsage usage = response.usage().get();
            promptTokens = (int) usage.inputTokens();
            completionTokens = (int) usage.outputTokens();
        }

        return new OpenAiResult(text.toString(), selectedModel, promptTokens, completionTokens, elapsed);
    }

    public void generateStream(String prompt, String model, Consumer<String> onToken) {
        String selectedModel = (model == null || model.isBlank()) ? defaultModel : model;

        if (mockEnabled) {
            String mockText = "Mock streaming response: " + prompt.substring(0, Math.min(60, prompt.length()));
            for (String word : mockText.split("(?<=\\s)")) {
                onToken.accept(word);
            }
            return;
        }

        ChatCompletionCreateParams params = ChatCompletionCreateParams.builder()
                .model(selectedModel)
                .addUserMessage(prompt)
                .build();

        try (StreamResponse<ChatCompletionChunk> stream = client.chat().completions().createStreaming(params)) {
            stream.stream().forEach(chunk ->
                    chunk.choices().forEach(choice ->
                            choice.delta().content().ifPresent(onToken)
                    )
            );
        }
    }
}
