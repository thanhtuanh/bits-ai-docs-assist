package com.bits.aidocassist.model;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import lombok.Data;

@Entity
@Data
public class Document {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 500)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String content;

    @Column(columnDefinition = "TEXT")
    private String summary;

    @Column(columnDefinition = "TEXT")
    private String keywords;

    @Column(columnDefinition = "TEXT")
    private String suggestedComponents;

    // 📄 Neue Felder für Datei-Metadaten
    @Column(length = 255)
    private String filename;

    @Column(length = 100)
    private String fileType;

    @Temporal(TemporalType.TIMESTAMP)
    private Date uploadDate;

    @Column(length = 255)
    private String sentiment;

    @Column(length = 255)
    private String tone;

    @Column(columnDefinition = "TEXT")
    private String recommendations;

    @Column
    private Double qualityScore;

    @Column(length = 100)
    private String documentType;

    @Column(length = 100)
    private String complexityLevel;

}
