# Portfolio AI Chatbot (Advanced RAG Assistant)

A premium, state-of-the-art AI Chatbot platform built with a **Hybrid RAG (Retrieval-Augmented Generation)** architecture. This application allows users to interact with an intelligent assistant that can answer general knowledge questions and perform deep analysis on uploaded documents.

![Portfolio Chatbot Preview](frontend/src/assets/preview.png) *(Note: Add your actual preview image here)*

## 🚀 Key Features

-   **Hybrid AI Engine**: Seamlessly switches between general LLM reasoning and document-specific retrieval (RAG) based on context.
-   **Intelligent Document Ingestion**: Supports PDF and Text file parsing with advanced chunking and embedding generation.
-   **Optimized Vector Search**: Custom Java-based **Cosine Similarity** algorithm for high-performance context retrieval without database-level complexity.
-   **ChatGPT-Inspired UX**:
    *   **Glassmorphism Aesthetic**: Modern dark mode with translucent panels and vivid accents.
    *   **Inline Editing**: Edit your prompts directly within the message bubble to refine your queries.
    *   **One-Click Copy**: Fast clipboard integration with visual click feedback and tooltips.
    *   **Typing Indicators**: Smooth micro-animations for real-time interaction feel.
-   **Multi-Session Management**: Robust sidebar logic for creating, naming, and isolating multiple chat conversations.
-   **Feedback & Correction System**: Suggest corrections to AI responses to help improve accuracy and build a localized knowledge base.

## 🛠️ Technology Stack

### Backend
-   **Java 21** & **Spring Boot 3.x**
-   **Spring Data JPA**: For reliable PostgreSQL integration.
-   **PostgreSQL**: Primary data store for chat history and document chunks.
-   **In-Memory Learning**: Custom Cosine Similarity logic implemented in Java for blazing-fast similarity searches.
-   **Mistral AI Integration**: Powered by state-of-the-art Large Language Models.

### Frontend
-   **Angular**: Component-based architecture for a scalable SPA.
-   **Vanilla CSS + Tailwind**: Custom-crafted Design System using CSS variables for a premium "Vivid" aesthetic.
-   **Outfit Typography**: Clean, modern font selection from Google Fonts.
-   **Marked.js**: Full Markdown support for AI responses (code blocks, tables, lists).

## 📦 Setup & Installation

### Prerequisites
-   JDK 21 or higher
-   Maven 3.x
-   Node.js (LTS version)
-   PostgreSQL instance

### Backend Setup
1.  Configure your database credentials in `src/main/resources/application.properties`.
2.  Add your AI API keys (e.g., Mistral/OpenAI) to the environment variables or configuration file.
3.  Run the application:
    ```bash
    mvn spring-boot:run
    ```

### Frontend Setup
1.  Navigate to the `frontend` directory:
    ```bash
    cd frontend
    ```
2.  Install dependencies:
    ```bash
    npm install
    ```
3.  Start the development server:
    ```bash
    npm start
    ```
    The app will be available at `http://localhost:4200`.

## 🏗️ Architecture
The system uses a **Session-Isolated RAG** model. When a document is uploaded, it is:
1.  **Parsed**: Text is extracted and cleaned.
2.  **Chunked**: Split into manageable semantic pieces.
3.  **Embedded**: Converted into high-dimensional vectors via an embedding model.
4.  **Stored**: Chunks and vectors are saved to PostgreSQL via standard JPA mappings.
5.  **Retrieved**: On query, the system calculates similarity in-memory for the specific session, ensuring data privacy and high speed.

## 📜 License
This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

---
*Built with ❤️ by [Your Name]*
