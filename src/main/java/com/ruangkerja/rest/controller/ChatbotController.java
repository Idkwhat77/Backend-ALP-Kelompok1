package com.ruangkerja.rest.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpEntity;
import org.springframework.http.MediaType;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.JsonNode;
import com.ruangkerja.rest.dto.ChatRequestDto;
import com.ruangkerja.rest.dto.ChatResponseDto;
import com.ruangkerja.rest.dto.ChatMessageDto;

import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;

@RestController
@RequestMapping("/api/chatbot")
@CrossOrigin(origins = "*")
public class ChatbotController {    // Constants
    private static final String GEMINI_API_URL = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.0-flash:generateContent";
    private static final String CONTENT_KEY = "content";
    private static final String PARTS_KEY = "parts";
    private static final String CANDIDATES_KEY = "candidates";
    private static final String TEXT_KEY = "text";

    @Value("${gemini.api.key}")
    private String geminiApiKey;
    
    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    // Custom exception for chatbot errors
    public static class ChatbotException extends Exception {
        public ChatbotException(String message) {
            super(message);
        }
        
        public ChatbotException(String message, Throwable cause) {
            super(message, cause);
        }
    }    @PostMapping("/chat")
    public ResponseEntity<ChatResponseDto> chat(@RequestBody ChatRequestDto request) {
        try {
            String userMessage = request.getMessage();
            List<ChatMessageDto> conversationHistory = request.getConversationHistory();
            
            if (userMessage == null || userMessage.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(new ChatResponseDto(false, null, "Message cannot be empty"));
            }

            // Create context for RuangKerja-specific responses
            String contextualPrompt = buildContextualPrompt(userMessage, conversationHistory);
            
            // Call Gemini API
            String geminiResponse = callGeminiAPI(contextualPrompt);
            
            return ResponseEntity.ok(new ChatResponseDto(true, geminiResponse));
            
        } catch (ChatbotException e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ChatResponseDto(false, null, "Chatbot error: " + e.getMessage()));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ChatResponseDto(false, null, "Internal server error: " + e.getMessage()));
        }
    }    private String buildContextualPrompt(String userMessage, List<ChatMessageDto> conversationHistory) {
        StringBuilder prompt = new StringBuilder();
        
        // UPDATED: More engaging system context
        prompt.append("You are an enthusiastic and knowledgeable AI career consultant for RuangKerja, Indonesia's premier professional networking platform. ");
        prompt.append("You're passionate about helping job seekers and professionals succeed in their careers. ");
        prompt.append("RuangKerja connects talented individuals with amazing opportunities across Indonesia. ");
        
        prompt.append("Your personality: ");
        prompt.append("- Friendly, encouraging, and motivational ");
        prompt.append("- Provide specific, actionable advice ");
        prompt.append("- Use examples and practical tips ");
        prompt.append("- Be conversational and engaging ");
        prompt.append("- Show enthusiasm for career growth ");
        prompt.append("- Ask follow-up questions when helpful ");
        
        prompt.append("Topics you excel at: ");
        prompt.append("- Job search strategies and hidden job markets ");
        prompt.append("- Resume optimization with ATS-friendly tips ");
        prompt.append("- Interview preparation and negotiation tactics ");
        prompt.append("- LinkedIn and professional networking ");
        prompt.append("- Career transitions and skill development ");
        prompt.append("- Industry insights and market trends ");
        prompt.append("- Personal branding and professional presence ");
        
        prompt.append("Always provide specific, actionable advice with examples. ");
        prompt.append("If the question isn't career-related, politely redirect with career insights. ");
        prompt.append("Keep responses engaging but concise (2-3 paragraphs max).\n\n");
        
        // Add conversation history for context
        if (conversationHistory != null && !conversationHistory.isEmpty()) {
            prompt.append("Conversation history:\n");
            for (ChatMessageDto message : conversationHistory) {
                String role = message.getRole();
                String content = message.getContent();
                
                if (role != null && content != null) {
                    prompt.append(role.equals("user") ? "User: " : "Assistant: ");
                    prompt.append(content).append("\n");
                }
            }
            prompt.append("\n");
        }
        
        // Add current user message
        prompt.append("Current user question: ").append(userMessage);
        prompt.append("\n\nProvide an engaging, helpful response with specific actionable advice:");
        
        return prompt.toString();
    }

    private String callGeminiAPI(String prompt) throws ChatbotException {
        try {
            // Prepare request body for Gemini API
            Map<String, Object> requestBody = new HashMap<>();
            
            // Create contents array
            List<Map<String, Object>> contents = new ArrayList<>();
            Map<String, Object> content = new HashMap<>();
            
            List<Map<String, String>> parts = new ArrayList<>();
            Map<String, String> part = new HashMap<>();
            part.put(TEXT_KEY, prompt);
            parts.add(part);
            
            content.put(PARTS_KEY, parts);
            contents.add(content);
            
            requestBody.put("contents", contents);
            
            // Add generation config for better responses
            Map<String, Object> generationConfig = new HashMap<>();
            generationConfig.put("temperature", 0.7);
            generationConfig.put("topK", 40);
            generationConfig.put("topP", 0.95);
            generationConfig.put("maxOutputTokens", 1024);
            requestBody.put("generationConfig", generationConfig);
            
            // Add safety settings
            List<Map<String, Object>> safetySettings = new ArrayList<>();
            String[] categories = {"HARM_CATEGORY_HARASSMENT", "HARM_CATEGORY_HATE_SPEECH", 
                                  "HARM_CATEGORY_SEXUALLY_EXPLICIT", "HARM_CATEGORY_DANGEROUS_CONTENT"};
            for (String category : categories) {
                Map<String, Object> setting = new HashMap<>();
                setting.put("category", category);
                setting.put("threshold", "BLOCK_MEDIUM_AND_ABOVE");
                safetySettings.add(setting);
            }
            requestBody.put("safetySettings", safetySettings);

            // Set headers
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            
            // Create HTTP entity
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);
            
            // Make API call
            String url = GEMINI_API_URL + "?key=" + geminiApiKey;
            ResponseEntity<String> response = restTemplate.postForEntity(url, entity, String.class);
            
            // Parse response
            JsonNode responseJson = objectMapper.readTree(response.getBody());
            
            if (responseJson.has(CANDIDATES_KEY) && responseJson.get(CANDIDATES_KEY).size() > 0) {
                JsonNode candidate = responseJson.get(CANDIDATES_KEY).get(0);
                if (candidate.has(CONTENT_KEY) && candidate.get(CONTENT_KEY).has(PARTS_KEY) && 
                    candidate.get(CONTENT_KEY).get(PARTS_KEY).size() > 0) {
                    return candidate.get(CONTENT_KEY).get(PARTS_KEY).get(0).get(TEXT_KEY).asText();
                }
            }
            
            // If we can't parse the response properly, return a fallback message
            throw new ChatbotException("Unable to parse response from Gemini API");
            
        } catch (Exception e) {
            throw new ChatbotException("Error calling Gemini API", e);
        }
    }

    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> health() {
        Map<String, String> response = new HashMap<>();
        response.put("status", "OK");
        response.put("service", "RuangKerja Chatbot");
        return ResponseEntity.ok(response);
    }
}
