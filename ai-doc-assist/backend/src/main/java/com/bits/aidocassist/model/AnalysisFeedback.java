package com.bits.aidocassist.model;

import java.time.LocalDateTime;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

import lombok.Data;

@Entity
@Data
public class AnalysisFeedback {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne
    @JoinColumn(name = "document_id")
    private Document document;
    
    // Bewertungen (1-5 Sterne)
    @Column(name = "summary_rating")
    private Integer summaryRating;
    
    @Column(name = "keywords_rating")
    private Integer keywordsRating;
    
    @Column(name = "components_rating")
    private Integer componentsRating;
    
    // Gesamtbewertung
    @Column(name = "overall_rating")
    private Integer overallRating;
    
    // Textuelle Bewertungen
    @Column(columnDefinition = "TEXT")
    private String userComments;
    
    @Column(columnDefinition = "TEXT")
    private String suggestedSummary;
    
    @Column(columnDefinition = "TEXT")
    private String suggestedKeywords;
    
    @Column(columnDefinition = "TEXT")
    private String suggestedComponents;
    
    // Hilfreich-Flags
    @Column(name = "summary_helpful")
    private Boolean summaryHelpful;
    
    @Column(name = "keywords_helpful")
    private Boolean keywordsHelpful;
    
    @Column(name = "components_helpful")
    private Boolean componentsHelpful;
    
    // Metadaten
    @Column(name = "user_ip")
    private String userIp;
    
    @Column(name = "user_agent")
    private String userAgent;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();
    
    // Verbesserungsvorschläge
    @Column(name = "improvement_category")
    private String improvementCategory; // "ACCURACY", "COMPLETENESS", "RELEVANCE"
    
    @Column(name = "document_type_suggestion")
    private String documentTypeSuggestion; // "BUSINESS", "TECHNICAL", "RESEARCH"
}
