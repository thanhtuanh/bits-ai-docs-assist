// src/app/document.service.ts
import { HttpClient, HttpEventType } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable, throwError } from 'rxjs';
import { catchError, map, retry } from 'rxjs/operators';
import { environment } from '../environments/environment';

@Injectable({
  providedIn: 'root'
})
export class DocumentService {
  private baseUrl = environment.apiUrl; // z.B. http://localhost:8080/api

  constructor(private http: HttpClient) {}

  createDocument(formData: FormData): Observable<any> {
    return this.http.post(`${this.baseUrl}/documents`, formData).pipe(
      catchError(this.handleError)
    );
  }

  analyzeText(text: string): Observable<any> {
    return this.http.post(`${this.baseUrl}/documents/analyze-text`, { text }).pipe(
      catchError(this.handleError)
    );
  }

  analyzeDocument(file: File, options: any): Promise<any> {
    const formData = new FormData();
    formData.append('file', file);
    formData.append('analysisOptions', JSON.stringify(options)); // ✅ richtiger Param-Name

    return this.http.post(`${this.baseUrl}/documents`, formData, {
      reportProgress: true,
      observe: 'events'
    }).pipe(
      map(event => (event.type === HttpEventType.Response ? event.body : null)),
      retry(2),
      catchError(this.handleError)
    ).toPromise();
  }

  getDocument(id: string): Observable<any> {
    return this.http.get(`${this.baseUrl}/documents/${id}`).pipe(
      catchError(this.handleError)
    );
  }

  deleteDocument(id: string): Observable<any> {
    return this.http.delete(`${this.baseUrl}/documents/${id}`).pipe(
      catchError(this.handleError)
    );
  }

  getDocumentHistory(): Observable<any[]> {
    return this.http.get<any[]>(`${this.baseUrl}/documents/history`).pipe(
      catchError(this.handleError)
    );
  }

  private handleError(error: any) {
    console.error('API Error:', error);
    let errorMessage = 'Ein unbekannter Fehler ist aufgetreten';

    if (error.error instanceof ErrorEvent) {
      errorMessage = `Client-Fehler: ${error.error.message}`;
    } else if (error.status) {
      switch (error.status) {
        case 400: errorMessage = 'Ungültige Anfrage. Bitte prüfen Sie die Eingaben.'; break;
        case 401: errorMessage = 'Nicht autorisiert. Bitte anmelden.'; break;
        case 413: errorMessage = 'Datei zu groß.'; break;
        case 429: errorMessage = 'Zu viele Anfragen. Bitte später erneut versuchen.'; break;
        case 500: errorMessage = 'Serverfehler. Bitte Support kontaktieren.'; break;
        default:  errorMessage = `Server-Fehler: ${error.status} - ${error.message}`; break;
      }
    }

    return throwError(() => new Error(errorMessage));
  }
}
