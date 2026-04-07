package com.ai.service;

import com.ai.entity.IntentRecognitionResult;
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
    private static final float TEMPERATURE = 0.2f;

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

                // 解析JSON
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

    /**
     * 构建意图识别的提示词
     *
     * @param userInput 用户输入的原始文本
     * @return 格式化后的提示词字符串
     */
    private String buildPrompt(String userInput) {
        return String.format(
            "你是一个医疗对话助手。请从患者的问题中识别意图（使用中文输出），并抽取出所有医疗相关实体（症状、检查、药品、治疗、科室），以JSON格式返回。 /no_think\n" +
            "**输出要求：**\n\n" +
            "- 只输出一个 JSON 对象，不要有任何额外文字、Markdown 或解释。\n" +
            "- JSON 格式：{\"intent\": \"意图\", \"entities\": [{\"type\": \"类型\", \"value\": \"值\"}]}\n" +
            "- 意图识别使用中文，意图类型有：描述症状、描述已有检查和治疗、询问所需检查、询问用药建议、描述基本信息、询问并发疾病、询问医疗建议、询问注意事项、询问病因、其他。\n" +
            "- 实体类型有：症状、检查、药品、治疗、科室。\n" +
            "- 如果用户没有提到任何实体，entities 为空数组。\n\n" +
            "**示例：**\n\n" +
            "患者问题：我最近咳嗽、发烧，喉咙痛\n" +
            "{\"intent\": \"描述症状\", \"entities\": [{\"type\": \"症状\", \"value\": \"咳嗽\"}, {\"type\": \"症状\", \"value\": \"发烧\"}, {\"type\": \"症状\", \"value\": \"喉咙痛\"}]}\n\n" +
            "患者问题：糖尿病要做什么检查\n" +
            "{\"intent\": \"询问所需检查\", \"entities\": [{\"type\": \"症状\", \"value\": \"糖尿病\"}]}\n\n" +
            "患者问题：肺炎应该吃什么药？\n" +
            "{\"intent\": \"询问用药建议\", \"entities\": [{\"type\": \"症状\", \"value\": \"肺炎\"}]}\n\n" +
            "患者问题：糖尿病有什么并发症？\n" +
            "{\"intent\": \"询问并发疾病\", \"entities\": [{\"type\": \"症状\", \"value\": \"糖尿病\"}]}\n\n" +
            "患者问题：肺炎有什么并发疾病？\n" +
            "{\"intent\": \"询问并发疾病\", \"entities\": [{\"type\": \"症状\", \"value\": \"肺炎\"}]}\n\n" +
            "患者问题：神经内科属于什么科室？\n" +
            "{\"intent\": \"询问所属科室\", \"entities\": [{\"type\": \"科室\", \"value\": \"神经内科\"}]}\n\n" +
            "---\n\n" +
            "患者问题：%s",
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
