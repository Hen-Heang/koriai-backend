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
                You are a Korean grammar and spelling correction assistant helping Korean learners improve their diary writing.
                Return ONLY valid JSON with this exact shape (no extra text):
                {
                  "correctedText": "...",
                  "explanation": "Brief overall summary of corrections in English (1-2 sentences)",
                  "grammarPoints": ["Point 1 in English", "Point 2 in English"],
                  "changes": [
                    {
                      "original": "exact original Korean fragment that was wrong",
                      "corrected": "the corrected Korean fragment",
                      "englishMeaning": "English meaning of the corrected phrase",
                      "reason": "Clear explanation in English of WHY this correction was needed (grammar rule, spelling rule, natural phrasing, etc.)"
                    }
                  ]
                }
                Rules:
                - "changes" must list EVERY individual correction made. If nothing was changed, return an empty array [].
                - "original" and "corrected" should be short fragments (the specific part that changed), not the whole sentence.
                - "reason" should teach the learner so they understand the rule, not just what changed.
                - All explanations must be in English to help Korean learners understand.

                Correct and explain this Korean text:
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
                    "example": "Short Korean example sentence using the term naturally",
                    "exampleTranslation": "English translation of the example sentence",
                    "tags": ["tag1", "tag2"]
                  }
                ]
                """.formatted(count, category, level);
    }

    public static String diaryFeedbackPrompt(String text) {
        return """
                You are a Korean diary coach helping a Korean learner improve their writing.
                Return ONLY valid JSON with this exact shape (no extra text):
                {
                  "correctedText": "The fully corrected Korean diary entry",
                  "feedback": "Encouraging overall feedback in English about tone, naturalness, and progress (2-3 sentences)",
                  "mood": "One word mood detected from the diary (e.g. happy, tired, excited, grateful)",
                  "grammarPoints": ["Grammar lesson point 1 in English", "Grammar lesson point 2 in English"],
                  "changes": [
                    {
                      "original": "exact original Korean fragment that was wrong",
                      "corrected": "the corrected Korean fragment",
                      "englishMeaning": "English meaning of the corrected phrase",
                      "reason": "Clear explanation in English of WHY this correction was needed"
                    }
                  ]
                }
                Rules:
                - "changes" must list EVERY individual correction. If nothing needed changing, return [].
                - "original" and "corrected" should be short fragments, not the whole sentence.
                - "reason" should help the learner understand the grammar/spelling rule.
                - All explanations in English to help the Korean learner.

                Improve and give feedback for this Korean diary entry:
                %s
                """.formatted(text);
    }
}
