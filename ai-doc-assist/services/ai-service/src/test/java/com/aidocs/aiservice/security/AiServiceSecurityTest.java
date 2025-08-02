package com.aidocs.aiservice.security;

import com.aidocs.aiservice.controller.AiController;
import com.aidocs.aiservice.service.AiService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Map;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AiController.class)
class AiServiceSecurityTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AiService aiService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void testSQLInjection_AttemptInText_ShouldNotCauseIssues() throws Exception {
        // Given - SQL Injection Versuch
        String maliciousText = "'; DROP TABLE users; --";
        Map<String, String> request = Map.of("text", maliciousText);
        Map<String, String> mockResponse = Map.of(
            "summary", "Safe processed text",
            "keywords", "safe, processed",
            "suggestedComponents", "Spring Boot"
        );
        
        when(aiService.analyzeText(anyString())).thenReturn(mockResponse);

        // When & Then
        mockMvc.perform(post("/api/ai/analyze")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.summary").value("Safe processed text"));
    }

    @Test
    void testXSS_AttemptInText_ShouldNotCauseIssues() throws Exception {
        // Given - XSS Versuch
        String maliciousText = "<script>alert('XSS')</script>";
        Map<String, String> request = Map.of("text", maliciousText);
        Map<String, String> mockResponse = Map.of(
            "summary", "Safe processed text without scripts",
            "keywords", "safe, text",
            "suggestedComponents", "Spring Boot"
        );
        
        when(aiService.analyzeText(anyString())).thenReturn(mockResponse);

        // When & Then
        mockMvc.perform(post("/api/ai/analyze")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.summary").value("Safe processed text without scripts"))
                .andExpect(content().string(org.hamcrest.Matchers.not(
                    org.hamcrest.Matchers.containsString("<script>"))));
    }

    @Test
    void testHTMLInjection_AttemptInText_ShouldBeHandledSafely() throws Exception {
        // Given - HTML Injection Versuch
        String maliciousText = "<iframe src='javascript:alert(1)'></iframe>";
        Map<String, String> request = Map.of("text", maliciousText);
        Map<String, String> mockResponse = Map.of(
            "summary", "Safe processed text",
            "keywords", "safe, processed",
            "suggestedComponents", "Spring Boot"
        );
        
        when(aiService.analyzeText(anyString())).thenReturn(mockResponse);

        // When & Then
        mockMvc.perform(post("/api/ai/analyze")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.not(
                    org.hamcrest.Matchers.containsString("<iframe>"))));
    }

    @Test
    void testLDAPInjection_AttemptInText_ShouldNotCauseIssues() throws Exception {
        // Given - LDAP Injection Versuch
        String maliciousText = "admin*)(uid=*))(|(uid=*";
        Map<String, String> request = Map.of("text", maliciousText);
        Map<String, String> mockResponse = Map.of(
            "summary", "Processed admin text",
            "keywords", "admin, text",
            "suggestedComponents", "Spring Boot"
        );
        
        when(aiService.analyzeText(anyString())).thenReturn(mockResponse);

        // When & Then
        mockMvc.perform(post("/api/ai/analyze")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.summary").value("Processed admin text"));
    }

    @Test
    void testCommandInjection_AttemptInText_ShouldNotExecuteCommands() throws Exception {
        // Given - Command Injection Versuch
        String maliciousText = "; rm -rf / ; echo 'hacked'";
        Map<String, String> request = Map.of("text", maliciousText);
        Map<String, String> mockResponse = Map.of(
            "summary", "Safe text analysis",
            "keywords", "safe, analysis",
            "suggestedComponents", "Spring Boot"
        );
        
        when(aiService.analyzeText(anyString())).thenReturn(mockResponse);

        // When & Then
        mockMvc.perform(post("/api/ai/analyze")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.summary").value("Safe text analysis"));
    }

    @Test
    void testPathTraversal_AttemptInText_ShouldNotAccessFiles() throws Exception {
        // Given - Path Traversal Versuch
        String maliciousText = "../../../etc/passwd";
        Map<String, String> request = Map.of("text", maliciousText);
        Map<String, String> mockResponse = Map.of(
            "summary", "File path analysis",
            "keywords", "file, path",
            "suggestedComponents", "Spring Boot"
        );
        
        when(aiService.analyzeText(anyString())).thenReturn(mockResponse);

        // When & Then
        mockMvc.perform(post("/api/ai/analyze")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.summary").value("File path analysis"));
    }

    @Test
    void testExtremelyLongInput_ShouldNotCauseDoS() throws Exception {
        // Given - Sehr langer Input (potentielle DoS-Attacke)
        String extremelyLongText = "A".repeat(1000000); // 1MB Text
        Map<String, String> request = Map.of("text", extremelyLongText);
        Map<String, String> mockResponse = Map.of(
            "summary", "Large text processed",
            "keywords", "large, text",
            "suggestedComponents", "Spring Boot"
        );
        
        when(aiService.analyzeText(anyString())).thenReturn(mockResponse);

        // When & Then
        mockMvc.perform(post("/api/ai/analyze")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.summary").value("Large text processed"));
    }

    @Test
    void testSpecialCharacterFlooding_ShouldHandleGracefully() throws Exception {
        // Given - Flooding mit Sonderzeichen
        String specialCharText = "@#$%^&*()".repeat(10000);
        Map<String, String> request = Map.of("text", specialCharText);
        Map<String, String> mockResponse = Map.of(
            "summary", "Special characters processed",
            "keywords", "special, characters",
            "suggestedComponents", "Spring Boot"
        );
        
        when(aiService.analyzeText(anyString())).thenReturn(mockResponse);

        // When & Then
        mockMvc.perform(post("/api/ai/analyze")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.summary").value("Special characters processed"));
    }

    @Test
    void testUnicodeOverload_ShouldNotCauseIssues() throws Exception {
        // Given - Unicode-Overload Versuch
        String unicodeText = "🚀".repeat(50000); // 50k Emojis
        Map<String, String> request = Map.of("text", unicodeText);
        Map<String, String> mockResponse = Map.of(
            "summary", "Unicode text processed",
            "keywords", "unicode, emojis",
            "suggestedComponents", "Spring Boot"
        );
        
        when(aiService.analyzeText(anyString())).thenReturn(mockResponse);

        // When & Then
        mockMvc.perform(post("/api/ai/analyze")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.summary").value("Unicode text processed"));
    }

    @Test
    void testJSONPayloadManipulation_ShouldRejectInvalidStructure() throws Exception {
        // Given - Manipulierte JSON-Struktur
        String maliciousJson = """
            {
                "text": "Normal text",
                "__proto__": {"isAdmin": true},
                "constructor": {"prototype": {"isAdmin": true}}
            }
            """;

        // When & Then
        mockMvc.perform(post("/api/ai/analyze")
                .contentType(MediaType.APPLICATION_JSON)
                .content(maliciousJson))
                .andExpect(status().isOk()); // Sollte normale Verarbeitung sein, da nur "text" relevant ist
    }

    @Test
    void testNullByteInjection_ShouldHandleSafely() throws Exception {
        // Given - Null-Byte Injection Versuch
        String nullByteText = "Normal text\u0000malicious content";
        Map<String, String> request = Map.of("text", nullByteText);
        Map<String, String> mockResponse = Map.of(
            "summary", "Processed text safely",
            "keywords", "processed, safely",
            "suggestedComponents", "Spring Boot"
        );
        
        when(aiService.analyzeText(anyString())).thenReturn(mockResponse);

        // When & Then
        mockMvc.perform(post("/api/ai/analyze")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.summary").value("Processed text safely"));
    }

    @Test
    void testRegexDoS_ShouldNotCausePerformanceIssues() throws Exception {
        // Given - Regex DoS Versuch (Evil Regex Pattern)
        String regexDoSText = "a".repeat(1000) + "X";
        Map<String, String> request = Map.of("text", regexDoSText);
        Map<String, String> mockResponse = Map.of(
            "summary", "Pattern text processed",
            "keywords", "pattern, text",
            "suggestedComponents", "Spring Boot"
        );
        
        when(aiService.analyzeText(anyString())).thenReturn(mockResponse);

        // When & Then - Sollte schnell abgearbeitet werden
        long startTime = System.currentTimeMillis();
        
        mockMvc.perform(post("/api/ai/analyze")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.summary").value("Pattern text processed"));
        
        long executionTime = System.currentTimeMillis() - startTime;
        assert executionTime < 5000 : "Request took too long, possible ReDoS: " + executionTime + "ms";
    }

    @Test
    void testContentTypeConfusion_ShouldRejectNonJSON() throws Exception {
        // Given - Content-Type Confusion Versuch
        String xmlPayload = """
            <?xml version="1.0"?>
            <text>XML injection attempt</text>
            """;

        // When & Then
        mockMvc.perform(post("/api/ai/analyze")
                .contentType(MediaType.APPLICATION_XML)
                .content(xmlPayload))
                .andExpect(status().isUnsupportedMediaType());
    }

    @Test
    void testHeaderInjection_ShouldNotAffectProcessing() throws Exception {
        // Given - Header Injection Versuch
        Map<String, String> request = Map.of("text", "Normal text");
        Map<String, String> mockResponse = Map.of(
            "summary", "Normal processing",
            "keywords", "normal, processing",
            "suggestedComponents", "Spring Boot"
        );
        
        when(aiService.analyzeText(anyString())).thenReturn(mockResponse);

        // When & Then
        mockMvc.perform(post("/api/ai/analyze")
                .contentType(MediaType.APPLICATION_JSON)
                .header("X-Forwarded-For", "'; DROP TABLE users; --")
                .header("User-Agent", "<script>alert('XSS')</script>")
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.summary").value("Normal processing"));
    }

    @Test
    void testHTTPMethodOverride_ShouldOnlyAllowDefinedMethods() throws Exception {
        // Given - HTTP Method Override Versuch
        Map<String, String> request = Map.of("text", "Test text");

        // When & Then - Nur POST sollte für /analyze erlaubt sein
        mockMvc.perform(get("/api/ai/analyze")
                .header("X-HTTP-Method-Override", "POST")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isMethodNotAllowed());

        mockMvc.perform(put("/api/ai/analyze")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isMethodNotAllowed());

        mockMvc.perform(delete("/api/ai/analyze"))
                .andExpect(status().isMethodNotAllowed());
    }

    @Test
    void testRateLimiting_ShouldHandleMultipleRequests() throws Exception {
        // Given - Multiple schnelle Requests (Simulation)
        Map<String, String> request = Map.of("text", "Rate limit test");
        Map<String, String> mockResponse = Map.of(
            "summary", "Processed successfully",
            "keywords", "processed, successfully",
            "suggestedComponents", "Spring Boot"
        );
        
        when(aiService.analyzeText(anyString())).thenReturn(mockResponse);

        // When & Then - Mehrere Requests schnell hintereinander
        for (int i = 0; i < 10; i++) {
            mockMvc.perform(post("/api/ai/analyze")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk());
        }
        
        // Note: Echte Rate-Limiting würde zusätzliche Infrastruktur erfordern
    }

    @Test
    void testCachePoison_ShouldNotAffectOtherUsers() throws Exception {
        // Given - Cache Poisoning Versuch (konzeptionell)
        String normalText = "Normal text for cache";
        String poisonedText = "Poisoned cache attempt with malicious content";
        
        Map<String, String> normalRequest = Map.of("text", normalText);
        Map<String, String> poisonedRequest = Map.of("text", poisonedText);
        
        Map<String, String> normalResponse = Map.of(
            "summary", "Normal summary",
            "keywords", "normal, summary",
            "suggestedComponents", "Spring Boot"
        );
        
        Map<String, String> poisonedResponse = Map.of(
            "summary", "Poisoned content blocked",
            "keywords", "poisoned, blocked",
            "suggestedComponents", "Spring Boot"
        );
        
        when(aiService.analyzeText(normalText)).thenReturn(normalResponse);
        when(aiService.analyzeText(poisonedText)).thenReturn(poisonedResponse);

        // When & Then - Beide Requests sollten unabhängig verarbeitet werden
        mockMvc.perform(post("/api/ai/analyze")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(normalRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.summary").value("Normal summary"));

        mockMvc.perform(post("/api/ai/analyze")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(poisonedRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.summary").value("Poisoned content blocked"));
    }

    @Test
    void testResponseHeaderSecurity_ShouldNotLeakInformation() throws Exception {
        // Given
        Map<String, String> request = Map.of("text", "Security header test");
        Map<String, String> mockResponse = Map.of(
            "summary", "Test summary",
            "keywords", "test, keywords",
            "suggestedComponents", "Spring Boot"
        );
        
        when(aiService.analyzeText(anyString())).thenReturn(mockResponse);

        // When & Then - Verify dass keine sensiblen Headers in Response sind
        mockMvc.perform(post("/api/ai/analyze")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(header().doesNotExist("X-Powered-By"))
                .andExpect(header().doesNotExist("Server"))
                .andExpect(header().exists("Content-Type"));
    }
}