package com.aidocs.aiservice.contract;

import com.aidocs.aiservice.controller.AiController;
import com.aidocs.aiservice.service.AiService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AiController.class)
class AiServiceContractTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AiService aiService;

    @Autowired
    private ObjectMapper objectMapper;

    /**
     * Contract Test: POST /api/ai/analyze
     * Verifies dass die API-Specification eingehalten wird
     */
    @Test
    void contract_analyzeEndpoint_ShouldMeetSpecification() throws Exception {
        // Given - Contract-konformer Request
        Map<String, String> request = Map.of("text", "Contract test text");
        Map<String, String> mockResponse = Map.of(
            "summary", "Contract test summary",
            "keywords", "contract, test, keywords",
            "suggestedComponents", "Spring Boot, React, PostgreSQL"
        );
        
        when(aiService.analyzeText(anyString())).thenReturn(mockResponse);

        // When & Then - Verify Contract Compliance
        MvcResult result = mockMvc.perform(post("/api/ai/analyze")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                // HTTP Status
                .andExpected(status().isOk())
                // Content Type
                .andExpect(header().string("Content-Type", "application/json"))
                // Response Structure
                .andExpect(jsonPath("$.summary").exists())
                .andExpect(jsonPath("$.keywords").exists())
                .andExpect(jsonPath("$.suggestedComponents").exists())
                // Response Values
                .andExpect(jsonPath("$.summary").isString())
                .andExpect(jsonPath("$.keywords").isString())
                .andExpect(jsonPath("$.suggestedComponents").isString())
                .andReturn();

        // Verify Response Body Structure
        String responseBody = result.getResponse().getContentAsString();
        Map<String, Object> responseMap = objectMapper.readValue(responseBody, Map.class);
        
        // Contract: Response muss genau diese 3 Felder enthalten
        assertEquals(3, responseMap.size());
        assertTrue(responseMap.containsKey("summary"));
        assertTrue(responseMap.containsKey("keywords"));
        assertTrue(responseMap.containsKey("suggestedComponents"));
        
        // Contract: Alle Werte müssen Strings sein
        assertTrue(responseMap.get("summary") instanceof String);
        assertTrue(responseMap.get("keywords") instanceof String);
        assertTrue(responseMap.get("suggestedComponents") instanceof String);
        
        // Contract: Keine leeren Werte
        assertFalse(((String) responseMap.get("summary")).isEmpty());
        assertFalse(((String) responseMap.get("keywords")).isEmpty());
        assertFalse(((String) responseMap.get("suggestedComponents")).isEmpty());
    }

    /**
     * Contract Test: Error Responses
     * Verifies dass Error Responses der Specification entsprechen
     */
    @Test
    void contract_analyzeEndpoint_ErrorResponse_ShouldMeetSpecification() throws Exception {
        // Given - Invalid Request (missing text)
        Map<String, String> invalidRequest = Map.of("content", "wrong field name");

        // When & Then - Verify Error Contract
        MvcResult result = mockMvc.perform(post("/api/ai/analyze")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(header().string("Content-Type", "application/json"))
                .andExpect(jsonPath("$.error").exists())
                .andExpect(jsonPath("$.error").isString())
                .andReturn();

        // Verify Error Response Structure
        String responseBody = result.getResponse().getContentAsString();
        Map<String, Object> errorResponse = objectMapper.readValue(responseBody, Map.class);
        
        // Contract: Error Response muss 'error' Feld enthalten
        assertTrue(errorResponse.containsKey("error"));
        assertEquals("Text is required", errorResponse.get("error"));
    }

    /**
     * Contract Test: GET /api/ai/health
     * Verifies Health Endpoint Contract
     */
    @Test
    void contract_healthEndpoint_ShouldMeetSpecification() throws Exception {
        // Given
        when(aiService.isOpenAiConfigured()).thenReturn(true);
        when(aiService.getModelInfo()).thenReturn("gpt-3.5-turbo-instruct");

        // When & Then
        MvcResult result = mockMvc.perform(get("/api/ai/health"))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Type", "application/json"))
                .andExpect(jsonPath("$.status").value("UP"))
                .andExpect(jsonPath("$.service").value("ai-service"))
                .andExpect(jsonPath("$.openai").exists())
                .andExpect(jsonPath("$.openai.configured").isBoolean())
                .andExpect(jsonPath("$.openai.model").isString())
                .andExpect(jsonPath("$.timestamp").isNumber())
                .andReturn();

        // Verify Health Response Structure
        String responseBody = result.getResponse().getContentAsString();
        Map<String, Object> healthResponse = objectMapper.readValue(responseBody, Map.class);
        
        // Contract: Health Response Structure
        assertEquals("UP", healthResponse.get("status"));
        assertEquals("ai-service", healthResponse.get("service"));
        assertTrue(healthResponse.containsKey("openai"));
        assertTrue(healthResponse.containsKey("timestamp"));
        
        @SuppressWarnings("unchecked")
        Map<String, Object> openaiInfo = (Map<String, Object>) healthResponse.get("openai");
        assertTrue(openaiInfo.containsKey("configured"));
        assertTrue(openaiInfo.containsKey("model"));
        assertTrue(openaiInfo.get("configured") instanceof Boolean);
        assertTrue(openaiInfo.get("model") instanceof String);
    }

    /**
     * Contract Test: GET /api/ai/info
     * Verifies Info Endpoint Contract
     */
    @Test
    void contract_infoEndpoint_ShouldMeetSpecification() throws Exception {
        // When & Then
        MvcResult result = mockMvc.perform(get("/api/ai/info"))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Type", "application/json"))
                .andExpect(jsonPath("$.service").value("AI Analysis Service"))
                .andExpect(jsonPath("$.version").value("1.0.0"))
                .andExpect(jsonPath("$.description").exists())
                .andExpect(jsonPath("$.endpoints").exists())
                .andExpect(jsonPath("$.features").exists())
                .andReturn();

        // Verify Info Response Structure
        String responseBody = result.getResponse().getContentAsString();
        Map<String, Object> infoResponse = objectMapper.readValue(responseBody, Map.class);
        
        // Contract: Required Fields
        assertTrue(infoResponse.containsKey("service"));
        assertTrue(infoResponse.containsKey("version"));
        assertTrue(infoResponse.containsKey("description"));
        assertTrue(infoResponse.containsKey("endpoints"));
        assertTrue(infoResponse.containsKey("features"));
        
        // Contract: Endpoints Structure
        @SuppressWarnings("unchecked")
        Map<String, Object> endpoints = (Map<String, Object>) infoResponse.get("endpoints");
        assertTrue(endpoints.containsKey("analyze"));
        assertTrue(endpoints.containsKey("health"));
        assertTrue(endpoints.containsKey("info"));
        
        // Contract: Features Structure
        @SuppressWarnings("unchecked")
        Map<String, Object> features = (Map<String, Object>) infoResponse.get("features");
        assertTrue(features.containsKey("caching"));
        assertTrue(features.containsKey("fallback"));
        assertTrue(features.containsKey("monitoring"));
    }

    /**
     * Contract Test: Request Validation
     * Verifies dass Input Validation der Specification entspricht
     */
    @Test
    void contract_requestValidation_ShouldEnforceConstraints() throws Exception {
        // Test Case 1: Empty Text
        Map<String, String> emptyTextRequest = Map.of("text", "");
        
        mockMvc.perform(post("/api/ai/analyze")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(emptyTextRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Text is required"));

        // Test Case 2: Whitespace Only Text
        Map<String, String> whitespaceRequest = Map.of("text", "   \t\n   ");
        
        mockMvc.perform(post("/api/ai/analyze")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(whitespaceRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Text is required"));

        // Test Case 3: Missing Text Field
        Map<String, String> missingFieldRequest = Map.of("content", "some content");
        
        mockMvc.perform(post("/api/ai/analyze")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(missingFieldRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Text is required"));
    }

    /**
     * Contract Test: Content Type Constraints
     * Verifies dass nur JSON akzeptiert wird
     */
    @Test
    void contract_contentType_ShouldOnlyAcceptJSON() throws Exception {
        String textContent = "text=test";
        
        // Test Case 1: Form Data - sollte abgelehnt werden
        mockMvc.perform(post("/api/ai/analyze")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .content(textContent))
                .andExpect(status().isUnsupportedMediaType());

        // Test Case 2: Plain Text - sollte abgelehnt werden
        mockMvc.perform(post("/api/ai/analyze")
                .contentType(MediaType.TEXT_PLAIN)
                .content(textContent))
                .andExpect(status().isUnsupportedMediaType());

        // Test Case 3: XML - sollte abgelehnt werden
        mockMvc.perform(post("/api/ai/analyze")
                .contentType(MediaType.APPLICATION_XML)
                .content("<text>test</text>"))
                .andExpected(status().isUnsupportedMediaType());
    }

    /**
     * Contract Test: HTTP Methods
     * Verifies dass nur erlaubte HTTP Methods akzeptiert werden
     */
    @Test
    void contract_httpMethods_ShouldOnlyAllowSpecified() throws Exception {
        // Test Case 1: GET auf /analyze - sollte nicht erlaubt sein
        mockMvc.perform(get("/api/ai/analyze"))
                .andExpect(status().isMethodNotAllowed());

        // Test Case 2: PUT auf /analyze - sollte nicht erlaubt sein
        mockMvc.perform(put("/api/ai/analyze"))
                .andExpect(status().isMethodNotAllowed());

        // Test Case 3: DELETE auf /analyze - sollte nicht erlaubt sein
        mockMvc.perform(delete("/api/ai/analyze"))
                .andExpect(status().isMethodNotAllowed());

        // Test Case 4: POST auf /health - sollte nicht erlaubt sein
        mockMvc.perform(post("/api/ai/health"))
                .andExpect(status().isMethodNotAllowed());

        // Test Case 5: POST auf /info - sollte nicht erlaubt sein
        mockMvc.perform(post("/api/ai/info"))
                .andExpect(status().isMethodNotAllowed());
    }

    /**
     * Contract Test: Response Headers
     * Verifies dass Response Headers der Specification entsprechen
     */
    @Test
    void contract_responseHeaders_ShouldMeetSpecification() throws Exception {
        Map<String, String> request = Map.of("text", "test");
        Map<String, String> mockResponse = Map.of(
            "summary", "test summary",
            "keywords", "test",
            "suggestedComponents", "Spring Boot"
        );
        
        when(aiService.analyzeText(anyString())).thenReturn(mockResponse);

        // When & Then
        mockMvc.perform(post("/api/ai/analyze")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                // Contract: Content-Type muss application/json sein
                .andExpect(header().string("Content-Type", "application/json"))
                // Contract: Keine Cache-Headers für dynamische Inhalte
                .andExpect(header().doesNotExist("Cache-Control"))
                // Contract: Keine sensiblen Server-Informationen
                .andExpect(header().doesNotExist("Server"))
                .andExpect(header().doesNotExist("X-Powered-By"));
    }

    /**
     * Contract Test: URL Patterns
     * Verifies dass URL Patterns der API Specification entsprechen
     */
    @Test
    void contract_urlPatterns_ShouldBeConsistent() throws Exception {
        // Test Case 1: Base Path /api/ai
        mockMvc.perform(get("/api/ai/health"))
                .andExpect(status().isOk());

        // Test Case 2: Trailing Slash sollte nicht erforderlich sein
        mockMvc.perform(get("/api/ai/health/"))
                .andExpect(status().isNotFound()); // Spring Boot Standard-Verhalten

        // Test Case 3: Case Sensitivity
        mockMvc.perform(get("/API/AI/HEALTH"))
                .andExpect(status().isNotFound()); // URLs sind case-sensitive

        // Test Case 4: Non-existent Endpoints
        mockMvc.perform(get("/api/ai/nonexistent"))
                .andExpect(status().isNotFound());
    }

    /**
     * Contract Test: Error Code Consistency
     * Verifies dass Error Codes konsistent verwendet werden
     */
    @Test
    void contract_errorCodes_ShouldBeConsistent() throws Exception {
        // Test Case 1: Bad Request (400) für invalid input
        Map<String, String> badRequest = Map.of("text", "");
        mockMvc.perform(post("/api/ai/analyze")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(badRequest)))
                .andExpected(status().isBadRequest()); // 400

        // Test Case 2: Method Not Allowed (405) für falsche HTTP Methode
        mockMvc.perform(get("/api/ai/analyze"))
                .andExpect(status().isMethodNotAllowed()); // 405

        // Test Case 3: Unsupported Media Type (415) für falschen Content-Type
        mockMvc.perform(post("/api/ai/analyze")
                .contentType(MediaType.TEXT_PLAIN)
                .content("plain text"))
                .andExpect(status().isUnsupportedMediaType()); // 415

        // Test Case 4: Not Found (404) für non-existent endpoints
        mockMvc.perform(get("/api/ai/nonexistent"))
                .andExpect(status().isNotFound()); // 404
    }

    /**
     * Contract Test: Response Time Constraints
     * Verifies dass die API innerhalb akzeptabler Zeit antwortet
     */
    @Test
    void contract_responseTime_ShouldMeetPerformanceRequirements() throws Exception {
        Map<String, String> request = Map.of("text", "performance test");
        Map<String, String> mockResponse = Map.of(
            "summary", "fast response",
            "keywords", "fast",
            "suggestedComponents", "Spring Boot"
        );
        
        when(aiService.analyzeText(anyString())).thenReturn(mockResponse);

        // When & Then - Response sollte unter 5 Sekunden sein (für Contract)
        long startTime = System.currentTimeMillis();
        
        mockMvc.perform(post("/api/ai/analyze")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
        
        long responseTime = System.currentTimeMillis() - startTime;
        assertTrue(responseTime < 5000, "Response time too slow for contract: " + responseTime + "ms");
    }
}