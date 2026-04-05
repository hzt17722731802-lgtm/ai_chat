package com.ai.service.neo4j;

import com.ai.entity.neo4j.Drug;
import com.ai.repository.neo4j.DrugRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class DrugService {

    private final DrugRepository drugRepository;

    public Drug createDrug(Drug drug) {
        return drugRepository.save(drug);
    }

    public List<Drug> getAllDrugs() {
        return drugRepository.findAllLimited();
    }

    public Optional<Drug> getDrugById(String id) {
        return drugRepository.findById(id);
    }

    public Optional<Drug> getDrugByName(String name) {
        return drugRepository.findByName(name);
    }

    public List<Drug> searchDrugs(String keyword) {
        return drugRepository.findByNameContaining(keyword);
    }

    public Drug updateDrug(String id, Drug drugDetails) {
        Drug drug = drugRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("药品不存在"));

        drug.setName(drugDetails.getName());

        return drugRepository.save(drug);
    }

    @Transactional
    public void deleteDrug(String id) {
        drugRepository.deleteById(id);
    }

    @Transactional
    public void deleteDrugByName(String name) {
        drugRepository.deleteByName(name);
    }
}
