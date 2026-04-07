package com.ai.service;

import com.ai.entity.vo.IntentRecognitionResult;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class IntentRecognitionService {

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    
    private static final String OLLAMA_API_URL = "http://localhost:11434/api/generate";
    private static final String MODEL_NAME = "medic_qwen0.6b";
    private static final float TEMPERATURE = 0.3f;

    public IntentRecognitionResult recognizeIntent(String userInput) {
        try {
            String prompt = buildPrompt(userInput);
            
            Map<String, Object> options = new HashMap<>();
            options.put("temperature", TEMPERATURE);

            
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("model", MODEL_NAME);
            requestBody.put("prompt", prompt);
            requestBody.put("stream", false);
            requestBody.put("options", options);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

            log.debug("调用 Ollama 进行意图识别，模型: {}, 温度: {}", MODEL_NAME, TEMPERATURE);
            
            Map<String, Object> response = restTemplate.postForObject(OLLAMA_API_URL, entity, Map.class);
            
            if (response != null && response.containsKey("response")) {
                String jsonResponse = (String) response.get("response");
                log.info("Ollama 原始响应: {}", jsonResponse);
                
                IntentRecognitionResult result = parseResponse(jsonResponse);
                return result;
            }
            
            log.warn("Ollama 返回结果为空，使用默认值");
            return createDefaultResult();
            
        } catch (Exception e) {
            log.error("意图识别异常", e);
            return createDefaultResult();
        }
    }

    private String buildPrompt(String userInput) {
        return String.format(
            userInput
        );
    }

    private IntentRecognitionResult parseResponse(String jsonResponse) {
        try {
            String cleanJson = extractJson(jsonResponse);
            if (cleanJson == null || cleanJson.isEmpty()) {
                log.warn("无法从响应中提取JSON");
                return createDefaultResult();
            }
            
            JsonNode jsonNode = objectMapper.readTree(cleanJson);
            
            IntentRecognitionResult result = new IntentRecognitionResult();
            
            if (jsonNode.has("intent")) {
                result.setIntent(jsonNode.get("intent").asText());
            } else {
                result.setIntent("其他咨询");
            }
            
            List<IntentRecognitionResult.Entity> entities = new ArrayList<>();
            if (jsonNode.has("entities") && jsonNode.get("entities").isArray()) {
                for (JsonNode entityNode : jsonNode.get("entities")) {
                    IntentRecognitionResult.Entity entity = new IntentRecognitionResult.Entity();
                    if (entityNode.has("type")) {
                        entity.setType(entityNode.get("type").asText());
                    }
                    if (entityNode.has("value")) {
                        entity.setValue(entityNode.get("value").asText());
                    }
                    entities.add(entity);
                }
            }
            result.setEntities(entities);
            
            return result;
        } catch (Exception e) {
            log.warn("解析JSON失败: {}", e.getMessage());
            return createDefaultResult();
        }
    }

    private String extractJson(String response) {
        if (response == null || response.isEmpty()) {
            return null;
        }
        
        response = response.trim();
        
        int start = response.indexOf('{');
        int end = response.lastIndexOf('}');
        
        if (start >= 0 && end > start) {
            return response.substring(start, end + 1);
        }
        
        return response;
    }

    private IntentRecognitionResult createDefaultResult() {
        IntentRecognitionResult result = new IntentRecognitionResult();
        result.setIntent("其他咨询");
        result.setEntities(new ArrayList<>());
        return result;
    }
}
