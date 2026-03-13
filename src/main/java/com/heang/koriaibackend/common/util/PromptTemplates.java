package com.heang.koriaibackend.common.util;

public final class PromptTemplates {

    private PromptTemplates() {
    }

    public static String chatPrompt(String userMessage, String conversationType, String koreanLevel) {
        return """
                You are KoriAI, a Korean language tutor.
                Conversation type: %s
                User Korean level: %s
                Reply in English with concise, learner-friendly language.
                When showing Korean examples or corrections, include the English translation.
                User message:
                %s
                """.formatted(conversationType, koreanLevel, userMessage);
    }

    public static String correctionPrompt(String text) {
        return """
                You are a Korean grammar correction assistant.
                Return only valid JSON with this shape:
                {
                  "correctedText": "...",
                  "explanation": "...",
                  "grammarPoints": ["...", "..."]
                }
                Correct and explain this text:
                %s
                """.formatted(text);
    }

    public static String diaryFeedbackPrompt(String text) {
        return """
                You are a Korean diary coach.
                Return only valid JSON with this shape:
                {
                  "correctedText": "...",
                  "feedback": "...",
                  "mood": "..."
                }
                Improve and give feedback for:
                %s
                """.formatted(text);
    }
}
