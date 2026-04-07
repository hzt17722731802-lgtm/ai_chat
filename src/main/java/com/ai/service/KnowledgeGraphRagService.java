package com.ai.service;

import com.ai.entity.IntentRecognitionResult;
import com.ai.entity.neo4j.*;
import com.ai.repository.neo4j.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.neo4j.core.Neo4jClient;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class KnowledgeGraphRagService {

    private final DiseaseRepository diseaseRepository;
    private final DrugRepository drugRepository;
    private final SymptomRepository symptomRepository;
    private final CheckRepository checkRepository;
    private final DepartmentRepository departmentRepository;
    private final Neo4jClient neo4jClient;

    public String buildRagContext(IntentRecognitionResult intentResult) {
        if (intentResult == null || intentResult.getEntities() == null || intentResult.getEntities().isEmpty()) {
            return "";
        }

        StringBuilder context = new StringBuilder();
        Set<String> processedDiseases = new HashSet<>();
        
        String intent = intentResult.getIntent();
        List<IntentRecognitionResult.Entity> entities = intentResult.getEntities();

        log.info("开始构建RAG上下文，意图: {}, 实体数量: {}", intent, entities.size());

        try {
            switch (intent) {
                case "询问所需检查":
                    handleCheckInquiry(entities, context, processedDiseases);
                    break;
                case "询问用药建议":
                    handleDrugInquiry(entities, context, processedDiseases);
                    break;
                case "描述症状":
                    handleSymptomDescription(entities, context, processedDiseases);
                    break;
                case "询问并发疾病":
                    handleAccompanyDiseaseInquiry(entities, context, processedDiseases);
                    break;
                case "询问所属科室":
                    handleDepartmentInquiry(entities, context, processedDiseases);
                    break;
                default:
                    handleDefaultIntent(entities, context, processedDiseases);
                    break;
            }
            
            processAllDiseaseEntities(entities, context, processedDiseases);
            
        } catch (Exception e) {
            log.error("构建RAG上下文失败", e);
        }

        return context.toString().trim();
    }

    private void processAllDiseaseEntities(List<IntentRecognitionResult.Entity> entities,
                                           StringBuilder context, Set<String> processedDiseases) {
        List<String> diseaseNames = entities.stream()
            .filter(e -> "疾病".equals(e.getType()))
            .map(IntentRecognitionResult.Entity::getValue)
            .distinct()
            .collect(Collectors.toList());

        for (String diseaseName : diseaseNames) {
            if (!processedDiseases.contains(diseaseName)) {
                log.info("处理疾病实体: {}", diseaseName);
                String diseaseInfo = getDiseaseFullInfo(diseaseName);
                if (!diseaseInfo.isEmpty()) {
                    context.append("【疾病信息】").append(diseaseInfo).append("\n");
                    processedDiseases.add(diseaseName);
                }
            }
        }
    }

    private void handleCheckInquiry(List<IntentRecognitionResult.Entity> entities, 
                                     StringBuilder context, Set<String> processedDiseases) {
        List<String> diseaseNames = entities.stream()
            .filter(e -> "疾病".equals(e.getType()))
            .map(IntentRecognitionResult.Entity::getValue)
            .collect(Collectors.toList());

        if (diseaseNames.isEmpty()) {
            return;
        }

        log.info("处理询问检查意图，疾病: {}", diseaseNames);

        for (String diseaseName : diseaseNames) {
            if (!processedDiseases.contains(diseaseName)) {
                Optional<Disease> diseaseOpt = diseaseRepository.findByName(diseaseName);
                if (diseaseOpt.isPresent()) {
                    context.append("【检查建议】").append("\n");
                    String diseaseInfo = getDiseaseFullInfo(diseaseName);
                    if (!diseaseInfo.isEmpty()) {
                        context.append(diseaseInfo).append("\n");
                        processedDiseases.add(diseaseName);
                    }
                }
            }
        }
    }

    private void handleDrugInquiry(List<IntentRecognitionResult.Entity> entities,
                                    StringBuilder context, Set<String> processedDiseases) {
        log.info("处理询问用药意图");
    }

    private void handleSymptomDescription(List<IntentRecognitionResult.Entity> entities,
                                          StringBuilder context, Set<String> processedDiseases) {
        List<String> symptomNames = entities.stream()
            .filter(e -> "症状".equals(e.getType()))
            .map(IntentRecognitionResult.Entity::getValue)
            .collect(Collectors.toList());

        if (symptomNames.isEmpty()) {
            return;
        }

        log.info("处理描述症状意图，症状: {}", symptomNames);

        List<Set<String>> diseaseSets = new ArrayList<>();
        for (String symptomName : symptomNames) {
            String query = "MATCH (s:Symptom {name: $symptomName})<-[:has_symptom]-(d:Disease) RETURN d.name as diseaseName";
            List<String> diseaseNames = executeSingleColumnQuery(query, "symptomName", symptomName);
            if (!diseaseNames.isEmpty()) {
                diseaseSets.add(new HashSet<>(diseaseNames));
                log.info("症状 '{}' 关联的疾病数量: {}", symptomName, diseaseNames.size());
            }
        }

        if (!diseaseSets.isEmpty()) {
            Set<String> intersectionDiseases = calculateIntersection(diseaseSets);
            if (!intersectionDiseases.isEmpty()) {
                context.append("【症状匹配】同时包含症状 ")
                       .append(String.join("、", symptomNames))
                       .append(" 的疾病有：")
                       .append(String.join("、", intersectionDiseases))
                       .append("\n");
                
                for (String diseaseName : intersectionDiseases) {
                    if (!processedDiseases.contains(diseaseName)) {
                        String diseaseInfo = getDiseaseFullInfo(diseaseName);
                        if (!diseaseInfo.isEmpty()) {
                            context.append(diseaseInfo).append("\n");
                            processedDiseases.add(diseaseName);
                        }
                    }
                }
            } else {
                log.warn("症状 {} 的交集为空", symptomNames);
            }
        }
    }

    private void handleAccompanyDiseaseInquiry(List<IntentRecognitionResult.Entity> entities,
                                                StringBuilder context, Set<String> processedDiseases) {
        log.info("处理询问并发疾病意图");
    }

    private void handleDepartmentInquiry(List<IntentRecognitionResult.Entity> entities,
                                         StringBuilder context, Set<String> processedDiseases) {
        List<String> departmentNames = entities.stream()
            .filter(e -> "科室".equals(e.getType()))
            .map(IntentRecognitionResult.Entity::getValue)
            .collect(Collectors.toList());

        if (departmentNames.isEmpty()) {
            return;
        }

        log.info("处理询问科室意图，科室: {}", departmentNames);

        for (String departmentName : departmentNames) {
            String query = "MATCH (dep:Department {name: $departmentName})-[:belongs_to]->(parent:Department) RETURN parent.name as parentName";
            List<String> parentDepartments = executeSingleColumnQuery(query, "departmentName", departmentName);
            
            if (!parentDepartments.isEmpty()) {
                context.append("【科室归属】").append(departmentName)
                       .append(" 属于 ").append(String.join("、", parentDepartments))
                       .append("\n");
            } else {
                context.append("【科室信息】未找到 ").append(departmentName)
                       .append(" 的上级科室信息\n");
            }
        }
    }

    private void handleDefaultIntent(List<IntentRecognitionResult.Entity> entities,
                                     StringBuilder context, Set<String> processedDiseases) {
        log.info("处理默认意图");
    }

    private Set<String> calculateIntersection(List<Set<String>> sets) {
        if (sets.isEmpty()) {
            return Collections.emptySet();
        }
        
        if (sets.size() == 1) {
            return sets.get(0);
        }

        Set<String> result = new HashSet<>(sets.get(0));
        for (int i = 1; i < sets.size(); i++) {
            result.retainAll(sets.get(i));
            if (result.isEmpty()) {
                break;
            }
        }
        
        return result;
    }

    private String getDiseaseFullInfo(String diseaseName) {
        StringBuilder info = new StringBuilder();

        Optional<Disease> diseaseOpt = diseaseRepository.findByName(diseaseName);
        if (!diseaseOpt.isPresent()) {
            return "";
        }

        Disease disease = diseaseOpt.get();
        info.append("- 疾病名称：").append(disease.getName());

        List<String> details = new ArrayList<>();

        if (disease.getDesc() != null && !disease.getDesc().isEmpty()) {
            details.add("描述：" + disease.getDesc());
        }

        if (disease.getCause() != null && !disease.getCause().isEmpty()) {
            details.add("病因：" + disease.getCause());
        }

        List<String> symptoms = getNodeNamesByRelationship(diseaseName, "has_symptom", "Symptom");
        if (!symptoms.isEmpty()) {
            details.add("常见症状：" + String.join("、", symptoms));
        }

        List<String> checks = getNodeNamesByRelationship(diseaseName, "need_check", "Check");
        if (!checks.isEmpty()) {
            details.add("推荐检查：" + String.join("、", checks));
        }

        List<String> commonDrugs = getNodeNamesByRelationship(diseaseName, "common_drug", "Drug");
        if (!commonDrugs.isEmpty()) {
            details.add("常用药物：" + String.join("、", commonDrugs));
        }

        List<String> recommandDrugs = getNodeNamesByRelationship(diseaseName, "recommand_drug", "Drug");
        if (!recommandDrugs.isEmpty()) {
            details.add("推荐药物：" + String.join("、", recommandDrugs));
        }

        List<String> accompanyDiseases = getNodeNamesByRelationship(diseaseName, "acompany_with", "Disease");
        if (!accompanyDiseases.isEmpty()) {
            details.add("并发疾病：" + String.join("、", accompanyDiseases));
        }

        List<String> departments = getNodeNamesByRelationship(diseaseName, "belongs_to", "Department");
        if (!departments.isEmpty()) {
            details.add("就诊科室：" + String.join("、", departments));
        }

        if (!details.isEmpty()) {
            info.append("\n  ").append(String.join("\n  ", details));
        }

        return info.toString();
    }

    private List<String> getNodeNamesByRelationship(String diseaseName, String relationshipType, String targetType) {
        String query = String.format(
                "MATCH (d:Disease {name: $diseaseName})-[:%s]->(n:%s) RETURN n.name as name",
                relationshipType, targetType
        );
        return executeSingleColumnQuery(query, "diseaseName", diseaseName);
    }

    private List<String> executeSingleColumnQuery(String query, String paramName, String paramValue) {
        try {
            return neo4jClient.query(query)
                    .bind(paramValue).to(paramName)
                    .fetchAs(Map.class)
                    .mappedBy((typeSystem, record) -> {
                        Map<String, Object> map = new HashMap<>();
                        record.keys().forEach(key -> {
                            map.put(key, record.get(key).asObject());
                        });
                        return map;
                    })
                    .all()
                    .stream()
                    .map(map -> {
                        for (Object value : map.values()) {
                            if (value != null) {
                                return value.toString();
                            }
                        }
                        return null;
                    })
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("执行查询失败: query={}, param={}", query, paramValue, e);
            return Collections.emptyList();
        }
    }
}
