// src/app/document.service.ts
import { HttpClient, HttpErrorResponse } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable, throwError } from 'rxjs';
import { catchError } from 'rxjs/operators';
import { environment } from '../environments/environment';

export interface Document {
  id?: number;
  title: string;
  content: string;
  summary: string;
  keywords: string;
  suggestedComponents: string;
}

@Injectable({
  providedIn: 'root'
})
export class DocumentService {
  private apiUrl = `${environment.apiUrl}/documents`;

  constructor(private http: HttpClient) {
    // 🔍 Debug: URL in Konsole ausgeben
    console.log('🔧 DocumentService initialized with API URL:', this.apiUrl);
    console.log('🔧 Environment:', environment);
  }

  createDocument(formData: FormData): Observable<Document> {
    console.log('📤 File Upload to:', this.apiUrl);
    return this.http.post<Document>(this.apiUrl, formData)
      .pipe(
        catchError(this.handleError)
      );
  }

  getDocument(id: number): Observable<Document> {
    return this.http.get<Document>(`${this.apiUrl}/${id}`)
      .pipe(
        catchError(this.handleError)
      );
  }

  analyzeText(text: string): Observable<Document> {
    const url = `${this.apiUrl}/analyze-text`;
    console.log('📤 Text Analysis to:', url);
    console.log('📝 Text:', text.substring(0, 50) + '...');
    
    return this.http.post<Document>(url, { text })
      .pipe(catchError(this.handleError));
  }

  private handleError(error: HttpErrorResponse) {
    let errorMessage = 'An error occurred';
    if (error.error instanceof ErrorEvent) {
      // Client-side error
      errorMessage = error.error.message;
    } else {
      // Server-side error
      errorMessage = `Error Code: ${error.status}\nMessage: ${error.message}`;
    }
    console.error('🚫 API Error:', errorMessage);
    console.error('🚫 Full Error:', error);
    return throwError(() => new Error(errorMessage));
  }
}