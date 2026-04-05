package com.ai.repository.neo4j;

import com.ai.entity.neo4j.Symptom;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.neo4j.repository.query.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SymptomRepository extends Neo4jRepository<Symptom, String> {

    Optional<Symptom> findByName(String name);

    List<Symptom> findByNameContaining(String keyword);

    @Query("MATCH (s:Symptom) RETURN s ORDER BY s.name LIMIT 100")
    List<Symptom> findAllLimited();
}
