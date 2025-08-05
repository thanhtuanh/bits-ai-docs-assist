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

  // 📂 Datei auswählen
  onFileSelected(event: Event) {
    const input = event.target as HTMLInputElement;
    if (input.files && input.files.length > 0) {
      this.selectedFile = input.files[0];
      this.uploadError = '';
    }
  }

  // 🚀 Datei hochladen & analysieren
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
        // Falls du lieber zur Summary-Seite willst:
        // this.router.navigate(['/summary', response.id]);
      },
      error: (error) => {
        this.isProcessing = false;
        this.uploadError = `Fehler beim Upload: ${error.message || 'Unbekannter Fehler'}`;
      }
    });
  }

  // 🤖 Text direkt analysieren - KORRIGIERT!
  analyzeText() {
    if (!this.inputText.trim()) {
      this.uploadError = 'Bitte Text eingeben.';
      return;
    }

    this.isProcessing = true;
    
    this.documentService.analyzeText(this.inputText).subscribe({
      next: (response: any) => {
        this.isProcessing = false;
        this.analysisResult = response;
        // Falls Weiterleitung gewünscht:
        // this.router.navigate(['/summary', response.id]);
      },
      error: (error) => {
        this.isProcessing = false;
        this.uploadError = `Fehler bei der Textanalyse: ${error.message || 'Unbekannter Fehler'}`;
      }
    });
  }
}