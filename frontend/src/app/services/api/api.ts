import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable } from 'rxjs';

@Injectable({
  providedIn: 'root',
})
export class Api {
  private http = inject(HttpClient);
  private backendUrl = 'http://localhost:8080/api';

  private activeSessionId: string | null = null;

  public setSessionId(id: string) {
    this.activeSessionId = id;
    localStorage.setItem('chatbot_session_id', id);
  }

  public getSessionId(): string {
    if (this.activeSessionId) return this.activeSessionId;
    let sessionId = localStorage.getItem('chatbot_session_id');
    if (!sessionId) {
      sessionId = 'session_' + Math.random().toString(36).substring(2, 15);
      localStorage.setItem('chatbot_session_id', sessionId);
    }
    this.activeSessionId = sessionId;
    return sessionId;
  }

  private getChatHeaders(): HttpHeaders {
    return new HttpHeaders({
      'X-Session-ID': this.getSessionId(),
      'Content-Type': 'application/json',
      'Accept': '*/*'
    });
  }

  // --- Chat API ---
  sendChatMessage(message: string): Observable<any> {
    return this.http.post(`${this.backendUrl}/chat`, { message }, { headers: this.getChatHeaders() });
  }

  sendChatFile(message: string, file: File): Observable<any> {
    const formData = new FormData();
    formData.append('message', message);
    formData.append('pdf', file);
    return this.http.post(`${this.backendUrl}/chat`, formData, { headers: this.getChatHeaders() });
  }

  getChatHistory(): Observable<any[]> {
    return this.http.get<any[]>(`${this.backendUrl}/history`, { headers: this.getChatHeaders() });
  }

  // --- Session History API ---
  getChatSessions(): Observable<any[]> {
    return this.http.get<any[]>(`${this.backendUrl}/history/sessions`);
  }

  deleteChatSession(sessionId: string): Observable<any> {
    return this.http.delete(`${this.backendUrl}/history/sessions/${sessionId}`);
  }

  // --- Mistral API ---
  uploadMistralFileAndMessage(file: File, message: string): Observable<any> {
    const formData = new FormData();
    formData.append('file', file);
    formData.append('message', message);
    return this.http.post(`${this.backendUrl}/mistral/upload`, formData, { responseType: 'text' as 'json' });
  }

  // --- RAG API ---
  ingestRagDocument(file: File): Observable<any> {
    const formData = new FormData();
    formData.append('file', file);
    return this.http.post(`${this.backendUrl}/rag/ingest`, formData, { responseType: 'text' as 'json' });
  }

  queryRag(query: string): Observable<any> {
    return this.http.post(`${this.backendUrl}/rag/query`, query, {
      headers: new HttpHeaders({ 'Content-Type': 'text/plain' }),
      responseType: 'text' as 'json'
    });
  }
}

