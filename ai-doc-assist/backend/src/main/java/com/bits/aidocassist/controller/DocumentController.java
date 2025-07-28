package com.bits.aidocassist.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import com.bits.aidocassist.model.Document;
import com.bits.aidocassist.service.DocumentService;
import com.bits.aidocassist.service.AiService;
import com.bits.aidocassist.util.PdfProcessor;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

@RestController
@RequestMapping("/api/documents")
@CrossOrigin(origins = "http://localhost:4200")
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
                // Für PDF-Dateien
                Path tempFile = Files.createTempFile("upload", ".pdf");
                file.transferTo(tempFile.toFile());
                content = PdfProcessor.extractTextFromPdf(tempFile.toFile());
                Files.deleteIfExists(tempFile);
            } else {
                // Für Text-Dateien
                content = new String(file.getBytes());
            }

            // AI-Services verwenden
            String summary = aiService.summarizeText(content);
            String keywords = aiService.extractKeywords(content);
            String suggestedComponents = aiService.suggestComponents(content);

            // Document erstellen
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

    @GetMapping("/{id}")
    public ResponseEntity<Document> getDocument(@PathVariable Long id) {
        Document document = documentService.getDocumentById(id);
        if (document == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(document);
    }
}