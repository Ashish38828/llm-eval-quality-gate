package com.architecture.evaluator.metrics;

import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.openai.OpenAiChatModel;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class HallucinationMetric {

    private static final Logger logger = LogManager.getLogger(HallucinationMetric.class);
    private final ChatLanguageModel evaluatorModel;

    public HallucinationMetric(String apiKey, String modelName) {
        // Temperature must be 0.0 for strict, mathematical evaluation without variance
        this.evaluatorModel = OpenAiChatModel.builder()
                .apiKey(apiKey)
                .modelName(modelName)
                .temperature(0.0)
                .build();
    }

    /**
     * Evaluates if the LLM's answer contains information not present in the Source Context.
     * Returns a score between 0.0 (Perfectly Grounded) and 1.0 (Complete Hallucination).
     */
    public double calculateScore(String sourceContext, String llmAnswer) {
        String prompt = String.format("""
            You are an impartial Quality Assurance AI designed to evaluate hallucination rates in RAG pipelines.
            Your task is to compare the "LLM Answer" against the "Source Context".
            
            Rule 1: If the LLM Answer contains facts, figures, or claims not explicitly present in the Source Context, it is a hallucination.
            Rule 2: Calculate a hallucination score between 0.0 and 1.0. 
            - 0.0 means the answer is 100%% derived from the context.
            - 1.0 means the answer is entirely fabricated.
            Rule 3: Return ONLY the numeric decimal value. Do not include any text, reasoning, or markdown formatting.
            
            Source Context: %s
            
            LLM Answer: %s
            """, sourceContext, llmAnswer);

        try {
            String result = evaluatorModel.generate(prompt).trim();
            return Double.parseDouble(result);
        } catch (NumberFormatException e) {
            logger.error("Evaluator failed to return a strict numeric value. Defaulting to maximum penalty (1.0).");
            return 1.0;
        }
    }
}
