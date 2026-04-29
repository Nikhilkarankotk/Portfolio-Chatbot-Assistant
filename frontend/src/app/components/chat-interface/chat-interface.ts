import { Component, inject, ElementRef, ViewChild, AfterViewChecked, ChangeDetectorRef, OnInit, Output, EventEmitter } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Api } from '../../services/api/api';
import { marked } from 'marked';

interface Message {
  id?: number;
  content: string;
  htmlContent?: string;
  role: 'user' | 'assistant';
  timestamp: Date;
  isError?: boolean;
  rating?: number; // 1 for thumbs up, -1 for thumbs down
  detectedLanguage?: string;
  isTranslated?: boolean;
}

@Component({
  selector: 'app-chat-interface',
  imports: [CommonModule, FormsModule],
  templateUrl: './chat-interface.html',
  styleUrl: './chat-interface.css'
})
export class ChatInterface implements AfterViewChecked, OnInit {
  private api = inject(Api);
  private cdr = inject(ChangeDetectorRef);

  @Output() messageSent = new EventEmitter<void>();

  messages: Message[] = [];
  userInput = '';
  selectedFile: File | null = null;
  isLoading = false;
  isIngesting = false;
  ingestSuccess = false;
  ingestError = '';

  // Correction Modal State
  showCorrectionModal = false;
  activeCorrectionMessage: Message | null = null;
  correctionText = '';

  @ViewChild('chatContainer') private chatContainer!: ElementRef;
  @ViewChild('fileInput') private fileInput!: ElementRef;

  ngOnInit() {
    this.loadHistory();
  }

  loadChat(sessionId: string) {
    this.api.setSessionId(sessionId);
    this.loadHistory();
  }

  loadHistory() {
    this.isLoading = true;
    this.api.getChatHistory().subscribe({
      next: async (history: any[]) => {
        this.messages = [];
        for (const msg of history ?? []) {
           const parsedContent = msg.content || '';
           const htmlParsed = msg.role === 'assistant' ? await marked.parse(parsedContent) : undefined;
           
           this.messages.push({
             id: msg.id,
             content: parsedContent,
             htmlContent: htmlParsed,
             role: msg.role as 'user' | 'assistant',
             timestamp: msg.timestamp ? new Date(msg.timestamp) : new Date(),
             detectedLanguage: msg.detectedLanguage,
             isTranslated: msg.isTranslated
           });
        }
        this.isLoading = false;
        this.cdr.detectChanges();
        setTimeout(() => this.scrollToBottom(), 100);
      },
      error: () => {
        this.isLoading = false;
        console.error('Failed to load chat history');
        this.cdr.detectChanges();
      }
    });
  }

  ngAfterViewChecked() {
    this.scrollToBottom();
  }

  sendMessage() {
    if (!this.userInput.trim() || this.isLoading) return;

    const text = this.userInput.trim();
    this.messages.push({ content: text, role: 'user', timestamp: new Date() });
    this.userInput = '';
    this.isLoading = true;

    this.api.sendChatMessage(text).subscribe({
      next: async (res: any) => {
        this.isLoading = false;
        try {
          const messageText = res.content || JSON.stringify(res);
          const htmlParsed = await marked.parse(messageText);

          this.messages.push({
            id: res.id,
            content: messageText,
            htmlContent: htmlParsed,
            role: 'assistant',
            timestamp: new Date(res.timestamp || new Date()),
            detectedLanguage: res.detectedLanguage,
            isTranslated: res.isTranslated
          });
          this.messageSent.emit();
        } catch (err: any) {
          console.error('Error parsing response in UI:', err);
          this.messages.push({
            content: `UI Parsing Error: ${err.message}`,
            role: 'assistant',
            isError: true,
            timestamp: new Date()
          });
        }
        this.cdr.detectChanges();
      },
      error: (err: any) => {
        this.isLoading = false;
        const errMessage = err.message || JSON.stringify(err);
        this.messages.push({
          content: `Backend Error: ${errMessage}`,
          role: 'assistant',
          isError: true,
          timestamp: new Date()
        });
        this.cdr.detectChanges();
      }
    });
  }

  // --- Feedback & Correction ---
  rateMessage(msg: Message, rating: number) {
    if (!msg.id) return;
    const newRating = msg.rating === rating ? 0 : rating;
    this.api.submitFeedback(msg.id, newRating).subscribe({
      next: () => msg.rating = newRating,
      error: (err) => console.error('Feedback failed', err)
    });
  }

  openCorrectionModal(msg: Message) {
    this.activeCorrectionMessage = msg;
    this.correctionText = msg.content;
    this.showCorrectionModal = true;
  }

  closeCorrectionModal() {
    this.showCorrectionModal = false;
    this.activeCorrectionMessage = null;
    this.correctionText = '';
  }

  submitCorrection() {
    if (!this.activeCorrectionMessage?.id || !this.correctionText.trim()) return;

    this.api.submitFeedback(
      this.activeCorrectionMessage.id, 
      this.activeCorrectionMessage.rating || 0, 
      this.correctionText
    ).subscribe({
      next: () => {
        this.closeCorrectionModal();
        alert('Thank you! Your correction has been recorded.');
      },
      error: (err) => {
        console.error('Correction failed', err);
        alert('Failed to submit correction.');
      }
    });
  }

  formatTime(date: Date): string {
    return new Date(date).toLocaleTimeString('en-US', { hour: '2-digit', minute: '2-digit' });
  }

  scrollToBottom(): void {
    if (this.chatContainer) {
      this.chatContainer.nativeElement.scrollTop = this.chatContainer.nativeElement.scrollHeight;
    }
  }

  onFileSelected(event: Event) {
    const input = event.target as HTMLInputElement;
    if (input.files && input.files[0]) {
      this.selectedFile = input.files[0];
    }
  }

  removeFile() {
    this.selectedFile = null;
    if (this.fileInput) this.fileInput.nativeElement.value = '';
  }

  ingestDocument() {
    if (!this.selectedFile) return;
    this.isIngesting = true;
    this.api.ingestRagDocument(this.selectedFile).subscribe({
      next: () => {
        this.isIngesting = false;
        this.ingestSuccess = true;
        this.selectedFile = null;
        this.cdr.detectChanges();
      },
      error: (err) => {
        this.isIngesting = false;
        this.ingestError = 'Failed to process document.';
        this.cdr.detectChanges();
      }
    });
  }

  clearChat() {
    this.messages = [];
    const newId = 'session_' + Math.random().toString(36).substring(2, 15);
    this.api.setSessionId(newId);
  }
}
