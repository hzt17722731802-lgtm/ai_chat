package com.ai.repository.neo4j;

import com.ai.entity.neo4j.Disease;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.neo4j.repository.query.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DiseaseRepository extends Neo4jRepository<Disease, String> {

    Optional<Disease> findByName(String name);

    List<Disease> findByNameContaining(String keyword);

    @Query("MATCH (d:Disease) RETURN d ORDER BY d.name LIMIT 100")
    List<Disease> findAllLimited();

    @Query("MATCH (d:Disease {name: $name}) DETACH DELETE d")
    void deleteByName(String name);
}
