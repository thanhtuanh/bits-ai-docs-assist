package com.aidocs.aiservice.controller;

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
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AiController.class)
class AiControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AiService aiService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void testAnalyzeText_ValidRequest_ShouldReturnAnalysis() throws Exception {
        // Given
        Map<String, String> request = Map.of("text", "Test text for analysis");
        Map<String, String> mockResponse = Map.of(
            "summary", "Test summary",
            "keywords", "test, analysis",
            "suggestedComponents", "Spring Boot, React"
        );
        
        when(aiService.analyzeText(anyString())).thenReturn(mockResponse);

        // When & Then
        MvcResult result = mockMvc.perform(post("/api/ai/analyze")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.summary").value("Test summary"))
                .andExpect(jsonPath("$.keywords").value("test, analysis"))
                .andExpect(jsonPath("$.suggestedComponents").value("Spring Boot, React"))
                .andReturn();

        // Verify Service wurde aufgerufen
        verify(aiService, times(1)).analyzeText("Test text for analysis");
    }

    @Test
    void testAnalyzeText_EmptyText_ShouldReturnBadRequest() throws Exception {
        // Given
        Map<String, String> request = Map.of("text", "");

        // When & Then
        mockMvc.perform(post("/api/ai/analyze")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Text is required"));

        // Verify Service wurde nicht aufgerufen
        verify(aiService, never()).analyzeText(anyString());
    }

    @Test
    void testAnalyzeText_NullText_ShouldReturnBadRequest() throws Exception {
        // Given
        Map<String, String> request = Map.of("other", "value"); // Kein "text" key

        // When & Then
        mockMvc.perform(post("/api/ai/analyze")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Text is required"));

        verify(aiService, never()).analyzeText(anyString());
    }

    @Test
    void testAnalyzeText_WhitespaceOnlyText_ShouldReturnBadRequest() throws Exception {
        // Given
        Map<String, String> request = Map.of("text", "   \t\n   ");

        // When & Then
        mockMvc.perform(post("/api/ai/analyze")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Text is required"));

        verify(aiService, never()).analyzeText(anyString());
    }

    @Test
    void testAnalyzeText_ServiceException_ShouldReturnInternalServerError() throws Exception {
        // Given
        Map<String, String> request = Map.of("text", "Test text");
        
        when(aiService.analyzeText(anyString())).thenThrow(new RuntimeException("Service error"));

        // When & Then
        mockMvc.perform(post("/api/ai/analyze")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.error").value("Analysis failed: Service error"));

        verify(aiService, times(1)).analyzeText("Test text");
    }

    @Test
    void testAnalyzeText_LongText_ShouldHandleSuccessfully() throws Exception {
        // Given
        String longText = "A".repeat(5000); // 5000 Zeichen langer Text
        Map<String, String> request = Map.of("text", longText);
        Map<String, String> mockResponse = Map.of(
            "summary", "Long text summary",
            "keywords", "long, text",
            "suggestedComponents", "Spring Boot"
        );
        
        when(aiService.analyzeText(anyString())).thenReturn(mockResponse);

        // When & Then
        mockMvc.perform(post("/api/ai/analyze")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.summary").value("Long text summary"));

        verify(aiService, times(1)).analyzeText(longText);
    }

    @Test
    void testHealth_ShouldReturnHealthStatus() throws Exception {
        // Given
        when(aiService.isOpenAiConfigured()).thenReturn(true);
        when(aiService.getModelInfo()).thenReturn("gpt-3.5-turbo-instruct");

        // When & Then
        MvcResult result = mockMvc.perform(get("/api/ai/health"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value("UP"))
                .andExpect(jsonPath("$.service").value("ai-service"))
                .andExpect(jsonPath("$.openai.configured").value(true))
                .andExpect(jsonPath("$.openai.model").value("gpt-3.5-turbo-instruct"))
                .andExpect(jsonPath("$.timestamp").exists())
                .andReturn();

        verify(aiService, times(1)).isOpenAiConfigured();
        verify(aiService, times(1)).getModelInfo();
    }

    @Test
    void testHealth_OpenAINotConfigured_ShouldShowFalse() throws Exception {
        // Given
        when(aiService.isOpenAiConfigured()).thenReturn(false);
        when(aiService.getModelInfo()).thenReturn("gpt-3.5-turbo-instruct");

        // When & Then
        mockMvc.perform(get("/api/ai/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP"))
                .andExpect(jsonPath("$.openai.configured").value(false));
    }

    @Test
    void testInfo_ShouldReturnServiceInfo() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/ai/info"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.service").value("AI Analysis Service"))
                .andExpect(jsonPath("$.version").value("1.0.0"))
                .andExpect(jsonPath("$.description").value("AI-powered text analysis with OpenAI integration"))
                .andExpect(jsonPath("$.endpoints.analyze").value("POST /api/ai/analyze"))
                .andExpect(jsonPath("$.endpoints.health").value("GET /api/ai/health"))
                .andExpect(jsonPath("$.endpoints.info").value("GET /api/ai/info"))
                .andExpect(jsonPath("$.features.caching").value("Redis-based result caching"))
                .andExpect(jsonPath("$.features.fallback").value("Local fallback when OpenAI unavailable"))
                .andExpect(jsonPath("$.features.monitoring").value("Prometheus metrics enabled"));

        // Verify keine Service Calls für Info
        verify(aiService, never()).analyzeText(anyString());
        verify(aiService, never()).isOpenAiConfigured();
    }

    @Test
    void testAnalyzeText_InvalidJson_ShouldReturnBadRequest() throws Exception {
        // Given
        String invalidJson = "{ invalid json }";

        // When & Then
        mockMvc.perform(post("/api/ai/analyze")
                .contentType(MediaType.APPLICATION_JSON)
                .content(invalidJson))
                .andExpect(status().isBadRequest());

        verify(aiService, never()).analyzeText(anyString());
    }

    @Test
    void testAnalyzeText_MissingContentType_ShouldReturnUnsupportedMediaType() throws Exception {
        // Given
        Map<String, String> request = Map.of("text", "Test text");

        // When & Then
        mockMvc.perform(post("/api/ai/analyze")
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnsupportedMediaType());

        verify(aiService, never()).analyzeText(anyString());
    }

    @Test
    void testAnalyzeText_SpecialCharacters_ShouldHandleCorrectly() throws Exception {
        // Given
        String textWithSpecialChars = "Text mit Umlauten: äöüß und Sonderzeichen: @#$%^&*()";
        Map<String, String> request = Map.of("text", textWithSpecialChars);
        Map<String, String> mockResponse = Map.of(
            "summary", "Summary with special chars",
            "keywords", "umlauten, sonderzeichen",
            "suggestedComponents", "Spring Boot"
        );
        
        when(aiService.analyzeText(anyString())).thenReturn(mockResponse);

        // When & Then
        mockMvc.perform(post("/api/ai/analyze")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.summary").value("Summary with special chars"));

        verify(aiService, times(1)).analyzeText(textWithSpecialChars);
    }

    @Test
    void testCorsHeaders_ShouldBeConfigured() throws Exception {
        // Given
        Map<String, String> request = Map.of("text", "Test text");
        Map<String, String> mockResponse = Map.of("summary", "Test");
        
        when(aiService.analyzeText(anyString())).thenReturn(mockResponse);

        // When & Then
        mockMvc.perform(post("/api/ai/analyze")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
                .header("Origin", "http://localhost:3000"))
                .andExpect(status().isOk());
        
        // Note: CORS Konfiguration würde normalerweise in separater Konfiguration getestet
    }
}