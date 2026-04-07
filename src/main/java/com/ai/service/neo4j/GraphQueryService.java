package com.ai.service.neo4j;

import com.ai.entity.vo.GraphDataVO;
import com.ai.entity.vo.GraphLinkVO;
import com.ai.entity.vo.GraphNodeVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.neo4j.core.Neo4jClient;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class GraphQueryService {
    
    private final Neo4jClient neo4jClient;
    
    public GraphDataVO getFullGraph() {
        try {
            String nodesQuery = "MATCH (n) RETURN id(n) as id, n.name as name, labels(n)[0] as type LIMIT 200";
            String relsQuery = "MATCH (n)-[r]->(m) RETURN id(n) as source, id(m) as target, type(r) as type, n.name as sourceName, m.name as targetName LIMIT 500";
            
            List<Map<String, Object>> nodeMaps = neo4jClient.query(nodesQuery)
                .fetchAs(Map.class)
                .mappedBy((typeSystem, record) -> {
                    Map<String, Object> map = new HashMap<>();
                    map.put("id", record.get("id").asObject());
                    map.put("name", record.get("name").asObject());
                    map.put("type", record.get("type").asObject());
                    return map;
                })
                .all()
                .stream()
                .map(map -> (Map<String, Object>) map)
                .collect(Collectors.toList());
            
            List<Map<String, Object>> relationshipMaps = neo4jClient.query(relsQuery)
                .fetchAs(Map.class)
                .mappedBy((typeSystem, record) -> {
                    Map<String, Object> map = new HashMap<>();
                    map.put("source", record.get("source").asObject());
                    map.put("target", record.get("target").asObject());
                    map.put("type", record.get("type").asObject());
                    map.put("sourceName", record.get("sourceName").asObject());
                    map.put("targetName", record.get("targetName").asObject());
                    return map;
                })
                .all()
                .stream()
                .map(map -> (Map<String, Object>) map)
                .collect(Collectors.toList());
            
            log.info("查询到节点数量: {}, 关系数量: {}", nodeMaps.size(), relationshipMaps.size());
            
            List<GraphNodeVO> nodes = nodeMaps.stream().map(map -> {
                GraphNodeVO node = new GraphNodeVO();
                node.setId(String.valueOf(map.get("id")));
                node.setName(map.get("name") != null ? (String) map.get("name") : "Unknown");
                node.setType(map.get("type") != null ? (String) map.get("type") : "Unknown");
                return node;
            }).collect(Collectors.toList());
            
            List<GraphLinkVO> links = relationshipMaps.stream().map(map -> {
                GraphLinkVO link = new GraphLinkVO();
                link.setSource(String.valueOf(map.get("source")));
                link.setTarget(String.valueOf(map.get("target")));
                link.setType(map.get("type") != null ? (String) map.get("type") : "UNKNOWN");
                link.setSourceName(map.get("sourceName") != null ? (String) map.get("sourceName") : "Unknown");
                link.setTargetName(map.get("targetName") != null ? (String) map.get("targetName") : "Unknown");
                return link;
            }).collect(Collectors.toList());
            
            return new GraphDataVO(nodes, links);
        } catch (Exception e) {
            log.error("获取完整图谱失败", e);
            throw new RuntimeException("获取图谱数据失败: " + e.getMessage(), e);
        }
    }
    
    public GraphDataVO getDiseaseGraph(String diseaseName) {
        try {
            String query = "MATCH (d:Disease {name: $diseaseName})-[r]->(related) " +
                          "RETURN id(d) as sourceId, id(related) as targetId, type(r) as type, " +
                          "d.name as sourceName, related.name as targetName, labels(related)[0] as targetType";
            
            List<Map<String, Object>> relationships = neo4jClient.query(query)
                .bind(diseaseName).to("diseaseName")
                .fetchAs(Map.class)
                .mappedBy((typeSystem, record) -> {
                    Map<String, Object> map = new HashMap<>();
                    map.put("sourceId", record.get("sourceId").asObject());
                    map.put("targetId", record.get("targetId").asObject());
                    map.put("type", record.get("type").asObject());
                    map.put("sourceName", record.get("sourceName").asObject());
                    map.put("targetName", record.get("targetName").asObject());
                    map.put("targetType", record.get("targetType").asObject());
                    return map;
                })
                .all()
                .stream()
                .map(map -> (Map<String, Object>) map)
                .collect(Collectors.toList());
            
            log.info("查询到疾病 '{}' 的出边关联关系数量: {}", diseaseName, relationships.size());
            
            Set<GraphNodeVO> nodes = new HashSet<>();
            List<GraphLinkVO> links = new ArrayList<>();
            
            for (Map<String, Object> rel : relationships) {
                String sourceId = String.valueOf(rel.get("sourceId"));
                String targetId = String.valueOf(rel.get("targetId"));
                String type = rel.get("type") != null ? (String) rel.get("type") : "UNKNOWN";
                String sourceName = rel.get("sourceName") != null ? (String) rel.get("sourceName") : "Unknown";
                String targetName = rel.get("targetName") != null ? (String) rel.get("targetName") : "Unknown";
                String targetType = rel.get("targetType") != null ? (String) rel.get("targetType") : "Unknown";
                
                nodes.add(new GraphNodeVO(sourceId, sourceName, "Disease", null));
                nodes.add(new GraphNodeVO(targetId, targetName, targetType, null));
                
                links.add(new GraphLinkVO(sourceId, targetId, type, sourceName, targetName));
            }
            
            return new GraphDataVO(new ArrayList<>(nodes), links);
        } catch (Exception e) {
            log.error("获取疾病图谱失败, diseaseName: {}", diseaseName, e);
            throw new RuntimeException("获取疾病图谱失败: " + e.getMessage(), e);
        }
    }
    
    public List<Map<String, Object>> getDiseaseRelatedInfo(String diseaseName) {
        try {
            Map<String, Object> result = new HashMap<>();
            
            result.put("recommand_drugs", fetchRecords(
                "MATCH (d:Disease {name: $diseaseName})-[:recommand_drug]->(dr:Drug) RETURN id(dr) as id, dr.name as name, 'Drug' as type",
                diseaseName
            ));
            result.put("common_drugs", fetchRecords(
                "MATCH (d:Disease {name: $diseaseName})-[:common_drug]->(dr:Drug) RETURN id(dr) as id, dr.name as name, 'Drug' as type",
                diseaseName
            ));
            result.put("symptoms", fetchRecords(
                "MATCH (d:Disease {name: $diseaseName})-[:has_symptom]->(s:Symptom) RETURN id(s) as id, s.name as name, 'Symptom' as type",
                diseaseName
            ));
            result.put("departments", fetchRecords(
                "MATCH (d:Disease {name: $diseaseName})-[:belongs_to]->(dep:Department) RETURN id(dep) as id, dep.name as name, 'Department' as type",
                diseaseName
            ));
            result.put("checks", fetchRecords(
                "MATCH (d:Disease {name: $diseaseName})-[:need_check]->(c:Check) RETURN id(c) as id, c.name as name, 'Check' as type",
                diseaseName
            ));
            result.put("accompany_diseases", fetchRecords(
                "MATCH (d:Disease {name: $diseaseName})-[:acompany_with]->(d2:Disease) RETURN id(d2) as id, d2.name as name, 'Disease' as type",
                diseaseName
            ));
            
            return Collections.singletonList(result);
        } catch (Exception e) {
            log.error("获取疾病关联信息失败, diseaseName: {}", diseaseName, e);
            throw new RuntimeException("获取疾病关联信息失败: " + e.getMessage(), e);
        }
    }
    
    private List<Map<String, Object>> fetchRecords(String query, String diseaseName) {
        return neo4jClient.query(query)
            .bind(diseaseName).to("diseaseName")
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
            .map(map -> (Map<String, Object>) map)
            .collect(Collectors.toList());
    }
}
