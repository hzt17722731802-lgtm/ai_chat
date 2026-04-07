package com.ai.entity;

import lombok.Data;
import java.util.List;

@Data
public class IntentRecognitionResult {
    private String intent;
    private List<Entity> entities;

    @Data
    public static class Entity {
        private String type;
        private String value;
    }
}
