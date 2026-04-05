package com.ai.service.neo4j;

import com.ai.entity.neo4j.Disease;
import com.ai.repository.neo4j.DiseaseRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class DiseaseService {

    private final DiseaseRepository diseaseRepository;

    public Disease createDisease(Disease disease) {
        return diseaseRepository.save(disease);
    }

    public List<Disease> getAllDiseases() {
        return diseaseRepository.findAllLimited();
    }

    public Optional<Disease> getDiseaseById(String id) {
        return diseaseRepository.findById(id);
    }

    public Optional<Disease> getDiseaseByName(String name) {
        return diseaseRepository.findByName(name);
    }

    public List<Disease> searchDiseases(String keyword) {
        return diseaseRepository.findByNameContaining(keyword);
    }

    public Disease updateDisease(String id, Disease diseaseDetails) {
        Disease disease = diseaseRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("疾病不存在"));

        disease.setName(diseaseDetails.getName());
        disease.setDesc(diseaseDetails.getDesc());
        disease.setCause(diseaseDetails.getCause());
        disease.setPrevent(diseaseDetails.getPrevent());
        disease.setEasyGet(diseaseDetails.getEasyGet());
        disease.setCureLasttime(diseaseDetails.getCureLasttime());
        disease.setCuredProb(diseaseDetails.getCuredProb());
        disease.setCureDepartment(diseaseDetails.getCureDepartment());
        disease.setCureWay(diseaseDetails.getCureWay());

        return diseaseRepository.save(disease);
    }

    @Transactional
    public void deleteDisease(String id) {
        diseaseRepository.deleteById(id);
    }

    @Transactional
    public void deleteDiseaseByName(String name) {
        diseaseRepository.deleteByName(name);
    }
}
