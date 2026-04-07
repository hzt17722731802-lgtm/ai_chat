package com.ai.service.neo4j;

import com.ai.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.neo4j.core.Neo4jClient;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class RelationshipService {
    
    private final Neo4jClient neo4jClient;
    
    public void createRecommandDrugRelationship(String diseaseName, String drugName) {
        log.info("开始创建关系: 疾病[{}] - recommand_drug -> 药品[{}]", diseaseName, drugName);
        
        validateNodeExists("Disease", diseaseName, "疾病");
        validateNodeExists("Drug", drugName, "药品");
        
        String cypher = "MATCH (d:Disease {name: $diseaseName}) " +
                       "MATCH (dr:Drug {name: $drugName}) " +
                       "MERGE (d)-[:recommand_drug]->(dr)";
        
        neo4jClient.query(cypher)
                .bind(diseaseName).to("diseaseName")
                .bind(drugName).to("drugName")
                .run();
        
        log.info("关系创建完成");
    }
    
    public void createCommonDrugRelationship(String diseaseName, String drugName) {
        log.info("开始创建关系: 疾病[{}] - common_drug -> 药品[{}]", diseaseName, drugName);
        
        validateNodeExists("Disease", diseaseName, "疾病");
        validateNodeExists("Drug", drugName, "药品");
        
        String cypher = "MATCH (d:Disease {name: $diseaseName}) " +
                       "MATCH (dr:Drug {name: $drugName}) " +
                       "MERGE (d)-[:common_drug]->(dr)";
        
        neo4jClient.query(cypher)
                .bind(diseaseName).to("diseaseName")
                .bind(drugName).to("drugName")
                .run();
        
        log.info("关系创建完成");
    }
    
    public void createHasSymptomRelationship(String diseaseName, String symptomName) {
        log.info("开始创建关系: 疾病[{}] - has_symptom -> 症状[{}]", diseaseName, symptomName);
        
        validateNodeExists("Disease", diseaseName, "疾病");
        validateNodeExists("Symptom", symptomName, "症状");
        
        String cypher = "MATCH (d:Disease {name: $diseaseName}) " +
                       "MATCH (s:Symptom {name: $symptomName}) " +
                       "MERGE (d)-[:has_symptom]->(s)";
        
        neo4jClient.query(cypher)
                .bind(diseaseName).to("diseaseName")
                .bind(symptomName).to("symptomName")
                .run();
        
        log.info("关系创建完成");
    }
    
    public void createBelongsToRelationship(String diseaseName, String departmentName) {
        log.info("开始创建关系: 疾病[{}] - belongs_to -> 科室[{}]", diseaseName, departmentName);
        
        validateNodeExists("Disease", diseaseName, "疾病");
        validateNodeExists("Department", departmentName, "科室");
        
        String cypher = "MATCH (d:Disease {name: $diseaseName}) " +
                       "MATCH (dep:Department {name: $departmentName}) " +
                       "MERGE (d)-[:belongs_to]->(dep)";
        
        neo4jClient.query(cypher)
                .bind(diseaseName).to("diseaseName")
                .bind(departmentName).to("departmentName")
                .run();
        
        log.info("关系创建完成");
    }
    
    public void createNeedCheckRelationship(String diseaseName, String checkName) {
        log.info("开始创建关系: 疾病[{}] - need_check -> 检查[{}]", diseaseName, checkName);
        
        validateNodeExists("Disease", diseaseName, "疾病");
        validateNodeExists("Check", checkName, "检查");
        
        String cypher = "MATCH (d:Disease {name: $diseaseName}) " +
                       "MATCH (c:Check {name: $checkName}) " +
                       "MERGE (d)-[:need_check]->(c)";
        
        neo4jClient.query(cypher)
                .bind(diseaseName).to("diseaseName")
                .bind(checkName).to("checkName")
                .run();
        
        log.info("关系创建完成");
    }
    
    public void createAcompanyWithRelationship(String diseaseName1, String diseaseName2) {
        log.info("开始创建关系: 疾病[{}] - acompany_with -> 疾病[{}]", diseaseName1, diseaseName2);
        
        validateNodeExists("Disease", diseaseName1, "疾病");
        validateNodeExists("Disease", diseaseName2, "疾病");
        
        String cypher = "MATCH (d1:Disease {name: $diseaseName1}) " +
                       "MATCH (d2:Disease {name: $diseaseName2}) " +
                       "MERGE (d1)-[:acompany_with]->(d2)";
        
        neo4jClient.query(cypher)
                .bind(diseaseName1).to("diseaseName1")
                .bind(diseaseName2).to("diseaseName2")
                .run();
        
        log.info("关系创建完成");
    }
    
    public void deleteRelationship(String startNodeName, String endNodeName, String relationshipType) {
        log.info("开始删除关系: [{}] - {} -> [{}]", startNodeName, relationshipType, endNodeName);
        
        if (nodeNotExistsByName(startNodeName)) {
            throw new BusinessException("起始节点不存在: " + startNodeName);
        }
        
        if (nodeNotExistsByName(endNodeName)) {
            throw new BusinessException("结束节点不存在: " + endNodeName);
        }
        
        String cypher = "MATCH (a)-[r:" + relationshipType + "]->(b) " +
                       "WHERE a.name = $startNodeName AND b.name = $endNodeName " +
                       "DELETE r";
        
        try {
            neo4jClient.query(cypher)
                    .bind(startNodeName).to("startNodeName")
                    .bind(endNodeName).to("endNodeName")
                    .run();
            
            log.info("关系删除完成");
        } catch (Exception e) {
            log.error("删除关系失败: [{}] - {} -> [{}]", startNodeName, relationshipType, endNodeName, e);
            throw new RuntimeException("删除关系失败: " + e.getMessage(), e);
        }
    }
    
    private void validateNodeExists(String label, String name, String nodeType) {
        if (!nodeExists(label, name)) {
            throw new BusinessException(nodeType + "节点不存在: " + name);
        }
    }
    
    private boolean nodeExists(String label, String name) {
        String cypher = "MATCH (n:" + label + " {name: $name}) RETURN count(n) > 0 as exists";
        
        Boolean exists = neo4jClient.query(cypher)
                .bind(name).to("name")
                .fetchAs(Boolean.class)
                .first()
                .orElse(false);
        
        if (!exists) {
            log.warn("节点不存在: label={}, name={}", label, name);
        }
        
        return exists;
    }
    
    private boolean nodeNotExistsByName(String name) {
        String cypher = "MATCH (n {name: $name}) RETURN count(n) > 0 as exists";
        
        Boolean exists = neo4jClient.query(cypher)
                .bind(name).to("name")
                .fetchAs(Boolean.class)
                .first()
                .orElse(false);
        
        if (!exists) {
            log.warn("节点不存在: name={}", name);
        }
        
        return exists;
    }
}
