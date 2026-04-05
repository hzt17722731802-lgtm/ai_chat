package com.ai.controller.admin;


import com.ai.common.Result;
import com.ai.entity.neo4j.*;
import com.ai.entity.vo.GraphDataVO;
import com.ai.service.neo4j.*;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/admin/neo4j")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class Neo4jController {

    private final DiseaseService diseaseService;
    private final DrugService drugService;
    private final GraphQueryService graphQueryService;
    private final RelationshipService relationshipService;

    @PostMapping("/disease")
    public Result<Disease> createDisease(@RequestBody Disease disease) {
        Disease created = diseaseService.createDisease(disease);
        return Result.success(created);
    }

    @GetMapping("/diseases")
    public Result<List<Disease>> getAllDiseases() {
        return Result.success(diseaseService.getAllDiseases());
    }

    @GetMapping("/disease/search")
    public Result<List<Disease>> searchDiseases(@RequestParam String keyword) {
        return Result.success(diseaseService.searchDiseases(keyword));
    }

    @GetMapping("/disease/{name}")
    public Result<Disease> getDiseaseByName(@PathVariable String name) {
        return diseaseService.getDiseaseByName(name)
                .map(Result::success)
                .orElse(Result.error("疾病不存在"));
    }

    @PutMapping("/disease/{id}")
    public Result<Disease> updateDisease(@PathVariable String id, @RequestBody Disease disease) {
        Disease updated = diseaseService.updateDisease(id, disease);
        return Result.success(updated);
    }

    @DeleteMapping("/disease/{id}")
    public Result<Void> deleteDisease(@PathVariable String id) {
        diseaseService.deleteDisease(id);
        return Result.success(null);
    }

    @PostMapping("/drug")
    public Result<Drug> createDrug(@RequestBody Drug drug) {
        Drug created = drugService.createDrug(drug);
        return Result.success(created);
    }

    @GetMapping("/drugs")
    public Result<List<Drug>> getAllDrugs() {
        return Result.success(drugService.getAllDrugs());
    }

    @GetMapping("/drug/search")
    public Result<List<Drug>> searchDrugs(@RequestParam String keyword) {
        return Result.success(drugService.searchDrugs(keyword));
    }

    @PutMapping("/drug/{id}")
    public Result<Drug> updateDrug(@PathVariable String id, @RequestBody Drug drug) {
        Drug updated = drugService.updateDrug(id, drug);
        return Result.success(updated);
    }

    @DeleteMapping("/drug/{id}")
    public Result<Void> deleteDrug(@PathVariable String id) {
        drugService.deleteDrug(id);
        return Result.success(null);
    }

    @GetMapping("/graph/full")
    public Result<GraphDataVO> getFullGraph() {
        return Result.success(graphQueryService.getFullGraph());
    }

    @GetMapping("/graph/disease")
    public Result<GraphDataVO> getDiseaseGraph(@RequestParam String diseaseName) {
        return Result.success(graphQueryService.getDiseaseGraph(diseaseName));
    }

    @GetMapping("/disease/{name}/info")
    public Result<List<Map<String, Object>>> getDiseaseRelatedInfo(@PathVariable String name) {
        return Result.success(graphQueryService.getDiseaseRelatedInfo(name));
    }

    @PostMapping("/relationship/recommand-drug")
    public Result<Void> createRecommandDrugRelationship(
            @RequestParam String diseaseName,
            @RequestParam String drugName) {
        relationshipService.createRecommandDrugRelationship(diseaseName, drugName);
        return Result.success(null);
    }

    @PostMapping("/relationship/common-drug")
    public Result<Void> createCommonDrugRelationship(
            @RequestParam String diseaseName,
            @RequestParam String drugName) {
        relationshipService.createCommonDrugRelationship(diseaseName, drugName);
        return Result.success(null);
    }

    @PostMapping("/relationship/has-symptom")
    public Result<Void> createHasSymptomRelationship(
            @RequestParam String diseaseName,
            @RequestParam String symptomName) {
        relationshipService.createHasSymptomRelationship(diseaseName, symptomName);
        return Result.success(null);
    }

    @PostMapping("/relationship/belongs-to")
    public Result<Void> createBelongsToRelationship(
            @RequestParam String diseaseName,
            @RequestParam String departmentName) {
        relationshipService.createBelongsToRelationship(diseaseName, departmentName);
        return Result.success(null);
    }

    @PostMapping("/relationship/need-check")
    public Result<Void> createNeedCheckRelationship(
            @RequestParam String diseaseName,
            @RequestParam String checkName) {
        relationshipService.createNeedCheckRelationship(diseaseName, checkName);
        return Result.success(null);
    }

    @PostMapping("/relationship/accompany-with")
    public Result<Void> createAcompanyWithRelationship(
            @RequestParam String diseaseName1,
            @RequestParam String diseaseName2) {
        relationshipService.createAcompanyWithRelationship(diseaseName1, diseaseName2);
        return Result.success(null);
    }

    @DeleteMapping("/relationship")
    public Result<Void> deleteRelationship(
            @RequestParam String startNodeName,
            @RequestParam String endNodeName,
            @RequestParam String relationshipType) {
        relationshipService.deleteRelationship(startNodeName, endNodeName, relationshipType);
        return Result.success(null);
    }
}
