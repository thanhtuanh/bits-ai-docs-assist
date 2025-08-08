import { Component } from '@angular/core';
import { Router } from '@angular/router';
import { HttpClient } from '@angular/common/http';
import { DocumentService } from '../document.service';
import { environment } from '../../environments/environment';

@Component({
  selector: 'app-document-upload',
  templateUrl: './document-upload.component.html',
  styleUrls: ['./document-upload.component.css']
})
export class DocumentUploadComponent {
  selectedFile: File | null = null;
  inputText: string = '';
  isProcessing = false;
  analysisResult: any = null;
  uploadError = '';

  constructor(
    private documentService: DocumentService,
    private http: HttpClient,
    private router: Router
  ) {}

  onFileSelected(event: Event) {
    const input = event.target as HTMLInputElement;
    if (input.files && input.files.length > 0) {
      this.selectedFile = input.files[0];
      this.uploadError = '';
    }
  }

  uploadDocument() {
    if (!this.selectedFile) {
      this.uploadError = 'Bitte wählen Sie eine Datei aus.';
      return;
    }

    this.isProcessing = true;
    const formData = new FormData();
    formData.append('file', this.selectedFile);

    this.documentService.createDocument(formData).subscribe({
      next: (response: any) => {
        this.isProcessing = false;
        // ✅ API Response korrekt verarbeiten
        if (response && response.document) {
          this.analysisResult = this.processApiResponse(response.document);
        } else {
          this.analysisResult = response; 
        }
      },
      error: (error: any) => {
        this.isProcessing = false;
        this.uploadError = `Fehler beim Upload: ${error.message || 'Unbekannter Fehler'}`;
      }
    });
  }

  analyzeText() {
    if (!this.inputText.trim()) {
      this.uploadError = 'Bitte Text eingeben.';
      return;
    }

    this.isProcessing = true;

    this.documentService.analyzeText(this.inputText).subscribe({
      next: (response: any) => {
        this.isProcessing = false;
        // ✅ API Response korrekt verarbeiten
        if (response && response.document) {
          this.analysisResult = this.processApiResponse(response.document);
        } else {
          this.analysisResult = response;
        }
      },
      error: (error: any) => {
        this.isProcessing = false;
        this.uploadError = `Fehler bei der Textanalyse: ${error.message || 'Unbekannter Fehler'}`;
      }
    });
  }

  // ✅ Neue Methode zur Verarbeitung der API-Antwort
  processApiResponse(document: any): any {
    const result = {
      summary: document.summary || '',
      keywords: this.parseKeywords(document.keywords),
      suggestedComponents: this.parseSuggestedComponents(document.suggestedComponents),
      qualityScore: document.qualityScore || 0,
      documentType: document.documentType || '',
      complexityLevel: document.complexityLevel || ''
    };

    console.log('Processed API Response:', result);
    return result;
  }

  // ✅ Keywords aus JSON-String oder Text extrahieren
  parseKeywords(keywordsString: string): string[] {
    if (!keywordsString) return [];
    
    try {
      // Versuche JSON zu parsen
      if (keywordsString.includes('```json')) {
        const jsonMatch = keywordsString.match(/```json\s*(\{[\s\S]*?\})\s*```/);
        if (jsonMatch) {
          const parsed = JSON.parse(jsonMatch[1]);
          const keywords: string[] = [];
          
          // Extrahiere alle Keywords aus der verschachtelten Struktur
          if (parsed.projekt) keywords.push(...parsed.projekt);
          if (parsed.technologien) {
            Object.values(parsed.technologien).forEach((tech: any) => {
              if (Array.isArray(tech)) keywords.push(...tech);
            });
          }
          if (parsed.konzepte) keywords.push(...parsed.konzepte);
          if (parsed.priorität_hoch) keywords.push(...parsed.priorität_hoch);
          
          return [...new Set(keywords)]; // Duplikate entfernen
        }
      }
      
      // Fallback: Als JSON parsen
      const parsed = JSON.parse(keywordsString);
      if (Array.isArray(parsed)) return parsed;
      
      // Wenn es ein Objekt ist, alle Werte sammeln
      const keywords: string[] = [];
      Object.values(parsed).forEach((value: any) => {
        if (Array.isArray(value)) keywords.push(...value);
        else if (typeof value === 'string') keywords.push(value);
      });
      return keywords;
      
    } catch (e) {
      // Fallback: String aufteilen
      return keywordsString.split(/[,;\n]/).map(k => k.trim()).filter(k => k.length > 0);
    }
  }

  // ✅ Suggested Components aus Text extrahieren
  parseSuggestedComponents(componentsString: string): string[] {
    if (!componentsString) return [];
    
    // Extrahiere Komponenten in eckigen Klammern [Component]
    const matches = componentsString.match(/\[([^\]]+)\]/g);
    if (matches) {
      return matches.map(match => match.replace(/[\[\]]/g, ''));
    }
    
    // Fallback: Nach Zeilen aufteilen und filtern
    return componentsString.split('\n')
      .map(line => line.trim())
      .filter(line => line.length > 0 && !line.startsWith('-'))
      .slice(0, 10); // Maximal 10 Komponenten
  }

  submitQuickFeedback(helpful: boolean) {
    console.log('Feedback:', helpful ? 'Hilfreich' : 'Nicht hilfreich');
    alert(helpful ? '👍 Danke für Ihr positives Feedback!' : '👎 Danke für Ihr Feedback. Wir arbeiten an Verbesserungen.');
  }
}
