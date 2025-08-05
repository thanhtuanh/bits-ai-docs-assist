
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
      next: (response) => {
        this.isProcessing = false;
        this.analysisResult = response; 
      },
      error: (error) => {
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
    
    // ✅ KORRIGIERT: DocumentService verwenden
    this.documentService.analyzeText(this.inputText).subscribe({
      next: (response: any) => {
        this.isProcessing = false;
        this.analysisResult = response;
      },
      error: (error) => {
        this.isProcessing = false;
        this.uploadError = `Fehler bei der Textanalyse: ${error.message || 'Unbekannter Fehler'}`;
      }
    });
  }

  submitQuickFeedback(helpful: boolean) {
    console.log('Feedback:', helpful ? 'Hilfreich' : 'Nicht hilfreich');
    // Hier könnte später echter Feedback-Service aufgerufen werden
    alert(helpful ? '👍 Danke für Ihr positives Feedback!' : '👎 Danke für Ihr Feedback. Wir arbeiten an Verbesserungen.');
  }
}