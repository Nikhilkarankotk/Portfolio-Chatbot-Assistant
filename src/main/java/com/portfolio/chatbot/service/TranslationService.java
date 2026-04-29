package com.portfolio.chatbot.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.portfolio.chatbot.dto.TranslationResult;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

@Service
public class TranslationService {

    private final MistralApiService mistralService;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private static final Logger log = LoggerFactory.getLogger(TranslationService.class);

    public TranslationService(MistralApiService mistralService) {
        this.mistralService = mistralService;
    }

    public TranslationResult detectAndTranslate(String text) {
        if (text == null || text.trim().length() < 3) {
            TranslationResult res = new TranslationResult();
            res.setLang("en");
            res.setLangName("English");
            res.setTranslatedText(text);
            return res;
        }

        String prompt = String.format(
                "Analyze the following text.\n" +
                        "1. Detect its base language (ISO 639-1 code).\n" +
                        "2. Detect the full language name and style. Be extremely careful to distinguish between South Indian languages like Telugu (te), Kannada (kn), and Tamil (ta). \n"
                        +
                        "   Telugu markers: 'Ninu/Ninnu/Nuvvu/Evaru/Enti/Anti'. Kannada markers: 'Yaaru/Avaru/Ninna/Esaru'.\n"
                        +
                        "3. If it is NOT English, translate it accurately to English.\n\n" +
                        "EXAMPLES:\n" +
                        "- 'am chestunnav' -> { 'lang': 'te', 'langName': 'Romanized Telugu', 'translatedText': 'What are you doing?' }\n"
                        +
                        "- 'ala unnav' -> { 'lang': 'te', 'langName': 'Romanized Telugu', 'translatedText': 'How are you?' }\n"
                        +
                        "- 'Ni peru anti' -> { 'lang': 'te', 'langName': 'Romanized Telugu', 'translatedText': 'What is your name?' }\n"
                        +
                        "- 'Ninnu evaru design chesaru' -> { 'lang': 'te', 'langName': 'Romanized Telugu', 'translatedText': 'Who designed you?' }\n"
                        +
                        "- 'Ninna hesaru enu' -> { 'lang': 'kn', 'langName': 'Romanized Kannada', 'translatedText': 'What is your name?' }\n\n"
                        +
                        "Return a raw JSON object ONLY: { 'lang': '...', 'langName': '...', 'translatedText': '...' }.\n\n"
                        +
                        "Text: %s",
                text);

        try {
            String response = mistralService.generateResponse(prompt);
            String json = response.replaceAll("```json", "").replaceAll("```", "").trim();
            return objectMapper.readValue(json, TranslationResult.class);
        } catch (Exception e) {
            log.error("Language detection failed, falling back to English", e);
            TranslationResult res = new TranslationResult();
            res.setLang("en");
            res.setLangName("English");
            res.setTranslatedText(text);
            return res;
        }
    }

    public String translateFromEnglish(String text, String langName) {
        if (langName == null || langName.toLowerCase().contains("english") || text == null)
            return text;

        String prompt = String.format(
                "System: You are a professional translator. Translate the following text from English to %s accurately. \n"
                        +
                        "STRICT RULES:\n" +
                        "1. Do NOT add any conversational filler, unrelated greetings, or hallucinations.\n" +
                        "2. Ensure the translation is faithful to the original English meaning.\n" +
                        "3. If the target is a Romanized language (like Romanized Telugu), use the English alphabet.\n"
                        +
                        "4. Reply ONLY with the translated text.\n\n" +
                        "Text to translate: %s",
                langName, text);
        String translation = mistralService.generateResponse(prompt);
        return translation != null ? translation.trim() : text;
    }
}
