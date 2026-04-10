package com.architecture.evaluator;

import com.architecture.evaluator.metrics.HallucinationMetric;
import io.github.cdimascio.dotenv.Dotenv;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class GatekeeperApp {

    private static final Logger logger = LogManager.getLogger(GatekeeperApp.class);

    public static void main(String[] args) {
        logger.info("Executing LLM Quality Gates...");

        Dotenv dotenv = Dotenv.load();
        String apiKey = dotenv.get("OPENAI_API_KEY");
        String modelName = dotenv.get("EVALUATOR_MODEL", "gpt-4o");
        double threshold = Double.parseDouble(dotenv.get("HALLUCINATION_THRESHOLD", "0.2"));

        if (apiKey == null || apiKey.equals("your_openai_api_key_here")) {
            logger.fatal("Missing OPENAI_API_KEY. Shutting down system.");
            System.exit(1);
        }

        HallucinationMetric evaluator = new HallucinationMetric(apiKey, modelName);

        // Simulated Data from a RAG Pipeline
        String secureContext = "The system uses OAuth2 for authentication. Database timeout is set to 30 seconds.";
        
        // Scenario 1: A good, grounded LLM response
        logger.info("\n--- SCENARIO 1: Grounded Response ---");
        String goodAnswer = "The system authentication is handled via OAuth2.";
        double score1 = evaluator.calculateScore(secureContext, goodAnswer);
        logger.info("Hallucination Score: {}", score1);
        if (score1 <= threshold) {
            logger.info("STATUS: PASSED (Pipeline Continues)");
        }

        // Scenario 2: A hallucinated response (LLM makes up facts about SAML and a 60s timeout)
        logger.info("\n--- SCENARIO 2: Hallucinated Response ---");
        String badAnswer = "The system uses OAuth2 and SAML for authentication. The database timeout is 60 seconds.";
        double score2 = evaluator.calculateScore(secureContext, badAnswer);
        
        if (score2 > threshold) {
            logger.warn("Hallucination Score: {}", score2);
            logger.error("STATUS: FAILED - Threshold Exceeded!");
            throw new RuntimeException("CI/CD Pipeline Blocked: LLM Hallucination Detected.");
        }
    }
}
