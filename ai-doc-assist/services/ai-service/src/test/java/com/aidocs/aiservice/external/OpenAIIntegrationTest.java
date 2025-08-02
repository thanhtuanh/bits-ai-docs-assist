package com.aidocs.aiservice.external;

import com.aidocs.aiservice.service.AiService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OpenAIIntegrationTest {

    @Mock
    private RestTemplate restTemplate;

    @Mock
    private StringRedisTemplate redisTemplate;

    @Mock
    private ValueOperations<String, String> valueOperations;

    private AiService aiService;

    @BeforeEach
    void setUp() {
        aiService = new AiService(restTemplate, redisTemplate);
        
        // Mock Redis
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get(anyString())).thenReturn(null); // Cache Miss

        // AI Service Konfiguration
        ReflectionTestUtils.setField(aiService, "openAiApiKey", "test-api-key");
        ReflectionTestUtils.setField(aiService, "model", "gpt-3.5-turbo-instruct");
    }

    @Test
    void testOpenAI_SuccessfulResponse_ShouldReturnAnalysis() {
        // Given
        String testText = "Moderne Webanwendung mit Spring Boot und React";
        
        // Mock erfolgreiche OpenAI Responses für Summary, Keywords, Components
        Map<String, Object> summaryResponse = createOpenAIResponse("Eine moderne Webanwendung mit aktuellen Technologien.");
        Map<String, Object> keywordsResponse = createOpenAIResponse("Webanwendung, Spring Boot, React, Modern");
        Map<String, Object> componentsResponse = createOpenAIResponse("Spring Boot, React, PostgreSQL, Docker");
        
        when(restTemplate.exchange(anyString(), any(), any(), eq(Map.class)))
            .thenReturn(new ResponseEntity<>(summaryResponse, HttpStatus.OK))
            .thenReturn(new ResponseEntity<>(keywordsResponse, HttpStatus.OK))
            .thenReturn(new ResponseEntity<>(componentsResponse, HttpStatus.OK));

        // When
        Map<String, String> result = aiService.analyzeText(testText);

        // Then
        assertNotNull(result);
        assertEquals("Eine moderne Webanwendung mit aktuellen Technologien.", result.get("summary"));
        assertEquals("Webanwendung, Spring Boot, React, Modern", result.get("keywords"));
        assertEquals("Spring Boot, React, PostgreSQL, Docker", result.get("suggestedComponents"));

        // Verify OpenAI wurde 3x aufgerufen
        verify(restTemplate, times(3)).exchange(anyString(), any(), any(), eq(Map.class));
    }

    @Test
    void testOpenAI_RateLimitError_ShouldUseFallback() {
        // Given
        String testText = "Test für Rate Limit Handling";
        
        // Mock Rate Limit Error (429)
        Map<String, Object> errorResponse = Map.of(
            "error", Map.of(
                "message", "Rate limit reached for requests",
                "type", "requests",
                "code", "rate_limit_exceeded"
            )
        );
        
        when(restTemplate.exchange(anyString(), any(), any(), eq(Map.class)))
            .thenReturn(new ResponseEntity<>(errorResponse, HttpStatus.TOO_MANY_REQUESTS));

        // When
        Map<String, String> result = aiService.analyzeText(testText);

        // Then
        assertNotNull(result);
        assertTrue(result.get("summary").contains("[Lokale Zusammenfassung - OpenAI nicht verfügbar]"));
        assertNotNull(result.get("keywords"));
        assertNotNull(result.get("suggestedComponents"));

        // Verify Fallback wurde verwendet
        verify(restTemplate, atLeast(1)).exchange(anyString(), any(), any(), eq(Map.class));
    }

    @Test
    void testOpenAI_InvalidApiKey_ShouldUseFallback() {
        // Given
        String testText = "Test für Invalid API Key";
        
        // Mock Authentication Error (401)
        Map<String, Object> errorResponse = Map.of(
            "error", Map.of(
                "message", "Invalid API key provided",
                "type", "invalid_request_error",
                "code", "invalid_api_key"
            )
        );
        
        when(restTemplate.exchange(anyString(), any(), any(), eq(Map.class)))
            .thenReturn(new ResponseEntity<>(errorResponse, HttpStatus.UNAUTHORIZED));

        // When
        Map<String, String> result = aiService.analyzeText(testText);

        // Then
        assertNotNull(result);
        assertTrue(result.get("summary").contains("[Lokale Zusammenfassung - OpenAI nicht verfügbar]"));
    }

    @Test
    void testOpenAI_ServerError_ShouldUseFallback() {
        // Given
        String testText = "Test für Server Error Handling";
        
        // Mock Server Error (500)
        Map<String, Object> errorResponse = Map.of(
            "error", Map.of(
                "message", "The server had an error while processing your request",
                "type", "server_error"
            )
        );
        
        when(restTemplate.exchange(anyString(), any(), any(), eq(Map.class)))
            .thenReturn(new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR));

        // When
        Map<String, String> result = aiService.analyzeText(testText);

        // Then
        assertNotNull(result);
        assertTrue(result.get("summary").contains("[Lokale Zusammenfassung - OpenAI nicht verfügbar]"));
    }

    @Test
    void testOpenAI_NetworkError_ShouldUseFallback() {
        // Given
        String testText = "Test für Network Error Handling";
        
        // Mock Network Error
        when(restTemplate.exchange(anyString(), any(), any(), eq(Map.class)))
            .thenThrow(new RestClientException("Connection timeout"));

        // When
        Map<String, String> result = aiService.analyzeText(testText);

        // Then
        assertNotNull(result);
        assertTrue(result.get("summary").contains("[Lokale Zusammenfassung - OpenAI nicht verfügbar]"));
    }

    @Test
    void testOpenAI_EmptyResponse_ShouldUseFallback() {
        // Given
        String testText = "Test für Empty Response";
        
        // Mock Empty Choices Response
        Map<String, Object> emptyResponse = Map.of(
            "choices", List.of(),
            "usage", Map.of("total_tokens", 0)
        );
        
        when(restTemplate.exchange(anyString(), any(), any(), eq(Map.class)))
            .thenReturn(new ResponseEntity<>(emptyResponse, HttpStatus.OK));

        // When
        Map<String, String> result = aiService.analyzeText(testText);

        // Then
        assertNotNull(result);
        assertTrue(result.get("summary").contains("[Lokale Zusammenfassung - OpenAI nicht verfügbar]"));
    }

    @Test
    void testOpenAI_MalformedResponse_ShouldUseFallback() {
        // Given
        String testText = "Test für Malformed Response";
        
        // Mock Malformed Response (missing 'choices' field)
        Map<String, Object> malformedResponse = Map.of(
            "usage", Map.of("total_tokens", 0)
        );
        
        when(restTemplate.exchange(anyString(), any(), any(), eq(Map.class)))
            .thenReturn(new ResponseEntity<>(malformedResponse, HttpStatus.OK));

        // When
        Map<String, String> result = aiService.analyzeText(testText);

        // Then
        assertNotNull(result);
        assertTrue(result.get("summary").contains("[Lokale Zusammenfassung - OpenAI nicht verfügbar]"));
    }

    @Test
    void testOpenAI_NoApiKey_ShouldUseFallback() {
        // Given
        String testText = "Test ohne API Key";
        
        // Service ohne API Key konfigurieren
        ReflectionTestUtils.setField(aiService, "openAiApiKey", "");

        // When
        Map<String, String> result = aiService.analyzeText(testText);

        // Then
        assertNotNull(result);
        assertTrue(result.get("summary").contains("[Lokale Zusammenfassung - OpenAI nicht verfügbar]"));
        
        // Verify dass keine OpenAI Calls gemacht wurden
        verify(restTemplate, never()).exchange(anyString(), any(), any(), eq(Map.class));
    }

    @Test
    void testOpenAI_PartialSuccess_ShouldHandleGracefully() {
        // Given
        String testText = "Test für Partial Success";
        
        // Mock: Summary erfolgreich, Keywords fehlschlägt, Components erfolgreich
        Map<String, Object> summaryResponse = createOpenAIResponse("Erfolgreiche Zusammenfassung");
        Map<String, Object> errorResponse = Map.of(
            "error", Map.of("message", "API Error")
        );
        Map<String, Object> componentsResponse = createOpenAIResponse("Spring Boot, React");
        
        when(restTemplate.exchange(anyString(), any(), any(), eq(Map.class)))
            .thenReturn(new ResponseEntity<>(summaryResponse, HttpStatus.OK))      // Summary OK
            .thenReturn(new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST)) // Keywords Error
            .thenReturn(new ResponseEntity<>(componentsResponse, HttpStatus.OK));  // Components OK

        // When
        Map<String, String> result = aiService.analyzeText(testText);

        // Then
        assertNotNull(result);
        // Bei teilweisen Fehlern sollte Fallback für alle verwendet werden
        assertTrue(result.get("summary").contains("[Lokale Zusammenfassung - OpenAI nicht verfügbar]"));
    }

    @Test
    void testOpenAI_SpecialCharacters_ShouldHandleCorrectly() {
        // Given
        String textWithSpecialChars = "Text mit Umlauten: äöüß und Symbolen: @#$%^&*() 🚀";
        
        // Mock erfolgreiche Response mit Sonderzeichen
        Map<String, Object> response = createOpenAIResponse("Response with special chars: äöüß 🚀");
        when(restTemplate.exchange(anyString(), any(), any(), eq(Map.class)))
            .thenReturn(new ResponseEntity<>(response, HttpStatus.OK));

        // When
        Map<String, String> result = aiService.analyzeText(textWithSpecialChars);

        // Then
        assertNotNull(result);
        assertEquals("Response with special chars: äöüß 🚀", result.get("summary"));
    }

    @Test
    void testOpenAI_LargeResponse_ShouldHandleCorrectly() {
        // Given
        String testText = "Test für Large Response";
        String largeResponse = "Very long response text. ".repeat(100); // ~2700 Zeichen
        
        Map<String, Object> response = createOpenAIResponse(largeResponse);
        when(restTemplate.exchange(anyString(), any(), any(), eq(Map.class)))
            .thenReturn(new ResponseEntity<>(response, HttpStatus.OK));

        // When
        Map<String, String> result = aiService.analyzeText(testText);

        // Then
        assertNotNull(result);
        assertEquals(largeResponse, result.get("summary"));
    }

    @Test
    void testOpenAI_MultipleConsecutiveCalls_ShouldWork() {
        // Given
        String testText1 = "Erster Test";
        String testText2 = "Zweiter Test";
        
        // Mock Responses für beide Texte
        when(restTemplate.exchange(anyString(), any(), any(), eq(Map.class)))
            .thenReturn(new ResponseEntity<>(createOpenAIResponse("Erste Antwort"), HttpStatus.OK))
            .thenReturn(new ResponseEntity<>(createOpenAIResponse("Keywords 1"), HttpStatus.OK))
            .thenReturn(new ResponseEntity<>(createOpenAIResponse("Components 1"), HttpStatus.OK))
            .thenReturn(new ResponseEntity<>(createOpenAIResponse("Zweite Antwort"), HttpStatus.OK))
            .thenReturn(new ResponseEntity<>(createOpenAIResponse("Keywords 2"), HttpStatus.OK))
            .thenReturn(new ResponseEntity<>(createOpenAIResponse("Components 2"), HttpStatus.OK));

        // When
        Map<String, String> result1 = aiService.analyzeText(testText1);
        Map<String, String> result2 = aiService.analyzeText(testText2);

        // Then
        assertNotNull(result1);
        assertNotNull(result2);
        assertEquals("Erste Antwort", result1.get("summary"));
        assertEquals("Zweite Antwort", result2.get("summary"));
        
        // Verify insgesamt 6 Calls (3 per Text)
        verify(restTemplate, times(6)).exchange(anyString(), any(), any(), eq(Map.class));
    }

    // Helper Method
    private Map<String, Object> createOpenAIResponse(String text) {
        return Map.of(
            "choices", List.of(Map.of("text", text)),
            "usage", Map.of("total_tokens", 150),
            "model", "gpt-3.5-turbo-instruct"
        );
    }
}