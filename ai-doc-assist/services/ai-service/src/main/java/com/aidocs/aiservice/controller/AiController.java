package com.aidocs.aiservice.controller;

import com.aidocs.aiservice.model.dto.AnalyzeRequest;
import com.aidocs.aiservice.service.AiService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.HashMap;

@RestController
@RequestMapping("/api/ai")
public class AiController {

    private static final Logger log = LoggerFactory.getLogger(AiController.class);
    private final AiService aiService;

    @Autowired
    public AiController(AiService aiService) {
        this.aiService = aiService;
    }

    @PostMapping("/analyze")
    public ResponseEntity<Map<String, String>> analyzeText(@RequestBody AnalyzeRequest request) {
        log.info("Received AI analysis request");

        String text = request.getText();
        if (text == null || text.trim().isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Text is required"));
        }

        try {
            Map<String, String> result = aiService.analyzeText(text);
            log.info("AI analysis completed successfully");
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("AI analysis failed: {}", e.getMessage());
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", "Analysis failed: " + e.getMessage()));
        }
    }

    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> health() {
        Map<String, Object> health = new HashMap<>();
        health.put("status", "UP");
        health.put("service", "ai-service");

        Map<String, Object> openAiInfo = new HashMap<>();
        openAiInfo.put("configured", aiService.isOpenAiConfigured());
        openAiInfo.put("model", aiService.getModelInfo());

        health.put("openai", openAiInfo);
        health.put("timestamp", System.currentTimeMillis());

        return ResponseEntity.ok(health);
    }


    @GetMapping("/info")
    public ResponseEntity<Map<String, Object>> info() {
        Map<String, Object> info = Map.of(
                "service", "AI Analysis Service",
                "version", "1.0.0",
                "description", "AI-powered text analysis with OpenAI integration",
                "endpoints", Map.of(
                        "analyze", "POST /api/ai/analyze",
                        "health", "GET /api/ai/health",
                        "info", "GET /api/ai/info"
                ),
                "features", Map.of(
                        "caching", "Redis-based result caching",
                        "fallback", "Local fallback when OpenAI unavailable",
                        "monitoring", "Prometheus metrics enabled"
                )
        );

        return ResponseEntity.ok(info);
    }
}
