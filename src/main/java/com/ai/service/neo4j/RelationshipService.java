package com.ai.service.neo4j;

import lombok.RequiredArgsConstructor;
import org.springframework.data.neo4j.core.Neo4jClient;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RelationshipService {
    
    private final Neo4jClient neo4jClient;
    
    public void createRecommandDrugRelationship(String diseaseName, String drugName) {
        String cypher = "MATCH (d:Disease {name: $diseaseName}) " +
                       "MATCH (dr:Drug {name: $drugName}) " +
                       "MERGE (d)-[:recommand_drug]->(dr)";
        
        neo4jClient.query(cypher)
                .bind(diseaseName).to("diseaseName")
                .bind(drugName).to("drugName")
                .run();
    }
    
    public void createCommonDrugRelationship(String diseaseName, String drugName) {
        String cypher = "MATCH (d:Disease {name: $diseaseName}) " +
                       "MATCH (dr:Drug {name: $drugName}) " +
                       "MERGE (d)-[:common_drug]->(dr)";
        
        neo4jClient.query(cypher)
                .bind(diseaseName).to("diseaseName")
                .bind(drugName).to("drugName")
                .run();
    }
    
    public void createHasSymptomRelationship(String diseaseName, String symptomName) {
        String cypher = "MATCH (d:Disease {name: $diseaseName}) " +
                       "MATCH (s:Symptom {name: $symptomName}) " +
                       "MERGE (d)-[:has_symptom]->(s)";
        
        neo4jClient.query(cypher)
                .bind(diseaseName).to("diseaseName")
                .bind(symptomName).to("symptomName")
                .run();
    }
    
    public void createBelongsToRelationship(String diseaseName, String departmentName) {
        String cypher = "MATCH (d:Disease {name: $diseaseName}) " +
                       "MATCH (dep:Department {name: $departmentName}) " +
                       "MERGE (d)-[:belongs_to]->(dep)";
        
        neo4jClient.query(cypher)
                .bind(diseaseName).to("diseaseName")
                .bind(departmentName).to("departmentName")
                .run();
    }
    
    public void createNeedCheckRelationship(String diseaseName, String checkName) {
        String cypher = "MATCH (d:Disease {name: $diseaseName}) " +
                       "MATCH (c:Check {name: $checkName}) " +
                       "MERGE (d)-[:need_check]->(c)";
        
        neo4jClient.query(cypher)
                .bind(diseaseName).to("diseaseName")
                .bind(checkName).to("checkName")
                .run();
    }
    
    public void createAcompanyWithRelationship(String diseaseName1, String diseaseName2) {
        String cypher = "MATCH (d1:Disease {name: $diseaseName1}) " +
                       "MATCH (d2:Disease {name: $diseaseName2}) " +
                       "MERGE (d1)-[:acompany_with]->(d2)";
        
        neo4jClient.query(cypher)
                .bind(diseaseName1).to("diseaseName1")
                .bind(diseaseName2).to("diseaseName2")
                .run();
    }
    
    public void deleteRelationship(String startNodeName, String endNodeName, String relationshipType) {
        String cypher = "MATCH (a)-[r:" + relationshipType + "]->(b) " +
                       "WHERE a.name = $startNodeName AND b.name = $endNodeName " +
                       "DELETE r";
        
        neo4jClient.query(cypher)
                .bind(startNodeName).to("startNodeName")
                .bind(endNodeName).to("endNodeName")
                .run();
    }
}
