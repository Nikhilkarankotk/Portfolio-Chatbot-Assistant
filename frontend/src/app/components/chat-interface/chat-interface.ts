import { Component, inject, ElementRef, ViewChild, AfterViewChecked, ChangeDetectorRef, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Api } from '../../services/api/api';
import { marked } from 'marked';

interface Message {
  content: string;
  htmlContent?: string;
  role: 'user' | 'assistant';
  timestamp: Date;
  isError?: boolean;
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

  messages: Message[] = [];
  userInput = '';
  selectedFile: File | null = null;
  isLoading = false;
  isIngesting = false;
  ingestSuccess = false;
  ingestError = '';

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
             content: parsedContent,
             htmlContent: htmlParsed,
             role: msg.role as 'user' | 'assistant',
             timestamp: msg.timestamp ? new Date(msg.timestamp) : new Date()
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

  scrollToBottom(): void {
    try {
      this.chatContainer.nativeElement.scrollTop =
        this.chatContainer.nativeElement.scrollHeight;
    } catch (err) {}
  }

  onFileSelected(event: Event) {
    const input = event.target as HTMLInputElement;
    if (input.files && input.files[0]) {
      this.selectedFile = input.files[0];
      this.ingestSuccess = false;
      this.ingestError = '';
    }
  }

  removeFile() {
    this.selectedFile = null;
    this.ingestSuccess = false;
    this.ingestError = '';
    if (this.fileInput) this.fileInput.nativeElement.value = '';
  }

  ingestDocument() {
    if (!this.selectedFile) return;
    this.isIngesting = true;
    this.ingestError = '';
    this.ingestSuccess = false;

    this.api.ingestRagDocument(this.selectedFile).subscribe({
      next: () => {
        this.isIngesting = false;
        this.ingestSuccess = true;
        this.selectedFile = null;
        if (this.fileInput) this.fileInput.nativeElement.value = '';
      },
      error: (err) => {
        this.isIngesting = false;
        this.ingestError = 'Failed to process document. Please try again.';
        console.error(err);
      }
    });
  }

  sendMessage() {
    if (!this.userInput.trim() || this.isLoading) return;

    const text = this.userInput.trim();
    this.messages.push({ content: text, role: 'user', timestamp: new Date() });
    this.userInput = '';
    this.isLoading = true;

    this.api.sendChatMessage(text).subscribe({
      next: async (res: any) => {
        console.log('Backend response received:', res);
        this.isLoading = false;
        try {
          let parsed = res;
          if (typeof res === 'string') {
            try {
              parsed = JSON.parse(res);
            } catch (e) {
              // Not JSON, keep as is
            }
          }

          const messageText = typeof parsed === 'string' 
            ? parsed 
            : (parsed && parsed.content ? parsed.content : JSON.stringify(parsed));

          const htmlParsed = await marked.parse(messageText);

          this.messages.push({
            content: messageText || '(Empty response)',
            htmlContent: htmlParsed,
            role: 'assistant',
            timestamp: new Date()
          });
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
        const errMessage = err.message ? err.message : JSON.stringify(err);
        this.messages.push({
          content: `Backend Error: ${errMessage}`,
          htmlContent: `Backend Error: ${errMessage}`,
          role: 'assistant',
          isError: true,
          timestamp: new Date()
        });
        alert('HTTP Request Failed: ' + errMessage);
        this.cdr.detectChanges();
      }
    });
  }

  clearChat() {
    this.messages = [];
    this.ingestSuccess = false;
    this.ingestError = '';
    // Generate new chat session ID
    const newId = 'session_' + Math.random().toString(36).substring(2, 15);
    this.api.setSessionId(newId);
  }

  formatTime(date: Date): string {
    return new Date(date).toLocaleTimeString('en-US', { hour: '2-digit', minute: '2-digit' });
  }
}
