import { Component } from '@angular/core';
import { Router } from '@angular/router';
import { DocumentService } from '../document.service';

@Component({
  selector: 'app-document-upload',
  templateUrl: './document-upload.component.html',
  styleUrls: ['./document-upload.component.css']
})
export class DocumentUploadComponent {
  selectedFile: File | null = null;
  isUploading = false;
  uploadMessage = '';
  uploadError = '';

  constructor(
    private documentService: DocumentService,
    private router: Router
  ) {}

  onFileSelected(event: Event) {
    const input = event.target as HTMLInputElement;
    if (input.files && input.files.length > 0) {
      this.selectedFile = input.files[0];
      this.uploadMessage = `Datei ausgewählt: ${this.selectedFile.name}`;
      this.uploadError = '';
    }
  }

  uploadDocument() {
    if (!this.selectedFile) {
      this.uploadError = 'Bitte wählen Sie eine Datei aus.';
      return;
    }

    this.isUploading = true;
    this.uploadMessage = 'Dokument wird hochgeladen und analysiert...';
    this.uploadError = '';

    const formData = new FormData();
    formData.append('file', this.selectedFile);

    this.documentService.createDocument(formData).subscribe({
      next: (response) => {
        console.log('Document uploaded successfully:', response);
        this.isUploading = false;
        this.uploadMessage = 'Upload erfolgreich! Weiterleitung zur Zusammenfassung...';
        
        // Weiterleitung zur Zusammenfassungsseite
        setTimeout(() => {
          this.router.navigate(['/summary', response.id]);
        }, 2000);
      },
      error: (error) => {
        console.error('Upload error:', error);
        this.isUploading = false;
        this.uploadError = 'Fehler beim Upload: ' + (error.message || 'Unbekannter Fehler');
        this.uploadMessage = '';
      }
    });
  }
}