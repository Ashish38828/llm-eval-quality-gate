# LLM Evaluation & Hallucination Gatekeeper

An automated CI/CD quality gate designed to evaluate Large Language Model (LLM) outputs mathematically. This framework prevents AI hallucinations, toxic responses, and ungrounded data from reaching production.

## The Architectural Problem
Traditional QA frameworks (Selenium, Playwright, RestAssured) are deterministic—they expect exact string matches or fixed JSON payloads. LLMs are non-deterministic. A standard assertion cannot mathematically prove if an LLM is hallucinating or providing an answer outside of its secure context window.

## The "LLM-as-a-Judge" Solution
This framework introduces an AI-native evaluation layer to the Software Testing Life Cycle (STLC). It utilizes an "Evaluator LLM" to score the outputs of the "Target LLM" based on strict mathematical thresholds.
* **Context Groundedness (Hallucination Detection):** Verifies that the LLM's answer is 100% derived from the provided secure context, penalizing fabricated information.
* **Pipeline Integration:** Designed to run as a JUnit 5 test suite within Jenkins/GitHub Actions, automatically failing the build if the hallucination score exceeds the enterprise threshold (e.g., > 0.2).

## Enterprise Tech Stack
* **Language:** Java 17+
* **Framework:** JUnit 5 (Jupiter)
* **AI Orchestration:** LangChain4j (`gpt-4o` acting as the Evaluator)
* **Observability:** Log4j2

## Sample CI/CD Execution

    2026-04-11 01:12:45 [main] INFO  com.architecture.evaluator.GatekeeperApp - Executing LLM Quality Gates...
    2026-04-11 01:12:45 [main] INFO  com.architecture.evaluator.GatekeeperApp - 
    --- SCENARIO 1: Grounded Response ---
    2026-04-11 01:12:47 [main] INFO  com.architecture.evaluator.GatekeeperApp - Hallucination Score: 0.0
    2026-04-11 01:12:47 [main] INFO  com.architecture.evaluator.GatekeeperApp - STATUS: PASSED (Pipeline Continues)
    
    2026-04-11 01:12:47 [main] INFO  com.architecture.evaluator.GatekeeperApp - 
    --- SCENARIO 2: Hallucinated Response ---
    2026-04-11 01:12:49 [main] WARN  com.architecture.evaluator.GatekeeperApp - Hallucination Score: 0.95
    2026-04-11 01:12:49 [main] ERROR com.architecture.evaluator.GatekeeperApp - STATUS: FAILED - Threshold Exceeded!
    Exception in thread "main" java.lang.RuntimeException: CI/CD Pipeline Blocked: LLM Hallucination Detected.

## Future Roadmap
Expanding the metrics suite to include **Toxicity Scoring** and **Prompt Injection Vulnerability Detection**, utilizing locally hosted open-source models (Ollama/Mistral) for zero-cost, high-frequency pipeline execution.
