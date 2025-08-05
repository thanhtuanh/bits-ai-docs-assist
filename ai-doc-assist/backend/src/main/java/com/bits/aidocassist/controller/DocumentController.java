package com.bits.aidocassist.controller;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.bits.aidocassist.model.Document;
import com.bits.aidocassist.service.AiService;
import com.bits.aidocassist.service.DocumentService;
import com.bits.aidocassist.util.PdfProcessor;

@CrossOrigin(origins = "http://localhost:4200")
@RestController
@RequestMapping("/api/documents")
public class DocumentController {

    @Autowired
    private DocumentService documentService;

    @Autowired
    private AiService aiService;

    @PostMapping
    public ResponseEntity<Document> createDocument(@RequestParam("file") MultipartFile file) {
        try {
            String content;
            String title = file.getOriginalFilename();

            if (title != null && title.toLowerCase().endsWith(".pdf")) {
                Path tempFile = Files.createTempFile("upload", ".pdf");
                file.transferTo(tempFile.toFile());
                content = PdfProcessor.extractTextFromPdf(tempFile.toFile());
                Files.deleteIfExists(tempFile);
            } else {
                content = new String(file.getBytes());
            }

            String summary = aiService.summarizeText(content);
            String keywords = aiService.extractKeywords(content);
            String suggestedComponents = aiService.suggestComponents(content);

            Document document = new Document();
            document.setTitle(title != null ? title : "Unbekanntes Dokument");
            document.setContent(content);
            document.setSummary(summary);
            document.setKeywords(keywords);
            document.setSuggestedComponents(suggestedComponents);

            Document savedDocument = documentService.saveDocument(document);
            return ResponseEntity.ok(savedDocument);

        } catch (IOException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping("/analyze-text")
    public ResponseEntity<Document> analyzeText(@RequestBody Map<String, String> payload) {
        String text = payload.get("text");
        if (text == null || text.trim().isEmpty()) {
            return ResponseEntity.badRequest().build();
        }

        String summary = aiService.summarizeText(text);
        String keywords = aiService.extractKeywords(text);
        String suggestedComponents = aiService.suggestComponents(text);

        Document document = new Document();
        document.setTitle("Direkt-Analyse");
        document.setContent(text);
        document.setSummary(summary);
        document.setKeywords(keywords);
        document.setSuggestedComponents(suggestedComponents);

        Document savedDocument = documentService.saveDocument(document);
        return ResponseEntity.ok(savedDocument);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Document> getDocument(@PathVariable Long id) {
        Document document = documentService.getDocumentById(id);
        if (document == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(document);
    }
}
