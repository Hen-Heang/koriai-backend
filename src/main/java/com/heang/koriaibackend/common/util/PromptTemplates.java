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

    public static String vocabGenerationPrompt(String category, String level, int count) {
        return """
                You are a Korean vocabulary teacher.
                Generate %d Korean vocabulary flashcards for the category "%s" suitable for a %s level learner.
                Return ONLY valid JSON array with this shape (no extra text):
                [
                  {
                    "term": "Korean word or phrase",
                    "meaning": "English meaning",
                    "example": "Short Korean example sentence",
                    "tags": ["tag1", "tag2"]
                  }
                ]
                """.formatted(count, category, level);
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
