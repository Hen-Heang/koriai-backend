package com.heang.koriaibackend.ai;

import com.heang.koriaibackend.ai.dto.OpenAiResult;
import com.openai.client.OpenAIClient;
import com.openai.core.RequestOptions;
import com.openai.core.http.StreamResponse;
import com.openai.models.Reasoning;
import com.openai.models.ReasoningEffort;
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

    // gpt-5 reasoning models default to "medium" effort, which adds a slow
    // internal reasoning pass before any output. Our tasks are short and
    // structured, so request "minimal" effort to cut latency dramatically.
    private static final long MAX_OUTPUT_TOKENS = 4096L;

    // Bound non-streaming calls (generate/analyze/evaluate) so a stalled OpenAI
    // response frees the request thread instead of hanging on the SDK's very
    // long default. Streaming (generateStream) is intentionally left untouched —
    // a long live token stream must not be cut off by a request timeout.
    private static final RequestOptions NON_STREAMING_OPTIONS = RequestOptions.builder()
            .timeout(java.time.Duration.ofSeconds(60))
            .build();

    private final OpenAIClient client;

    @Value("${openai.model:gpt-5-mini}")
    private String defaultModel;

    @Value("${openai.mock-enabled:false}")
    private boolean mockEnabled;

    // Reasoning params are only valid for gpt-5+ models; sending them to a
    // gpt-4o model would be rejected, so guard on the model family.
    private static boolean isReasoningModel(String model) {
        return model != null && model.startsWith("gpt-5");
    }

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
        ResponseCreateParams.Builder paramsBuilder = ResponseCreateParams.builder()
                .model(selectedModel)
                .input(prompt)
                .maxOutputTokens(MAX_OUTPUT_TOKENS);
        if (isReasoningModel(selectedModel)) {
            paramsBuilder.reasoning(Reasoning.builder().effort(ReasoningEffort.MINIMAL).build());
        }
        Response response = client.responses().create(paramsBuilder.build(), NON_STREAMING_OPTIONS);
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

        ChatCompletionCreateParams.Builder paramsBuilder = ChatCompletionCreateParams.builder()
                .model(selectedModel)
                .addUserMessage(prompt)
                .maxCompletionTokens(MAX_OUTPUT_TOKENS);
        if (isReasoningModel(selectedModel)) {
            paramsBuilder.reasoningEffort(ReasoningEffort.MINIMAL);
        }

        try (StreamResponse<ChatCompletionChunk> stream = client.chat().completions().createStreaming(paramsBuilder.build())) {
            stream.stream().forEach(chunk ->
                    chunk.choices().forEach(choice ->
                            choice.delta().content().ifPresent(onToken)
                    )
            );
        }
    }
}
