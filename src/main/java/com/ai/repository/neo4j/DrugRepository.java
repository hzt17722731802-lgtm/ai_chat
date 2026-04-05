package com.ai.repository.neo4j;

import com.ai.entity.neo4j.Drug;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.neo4j.repository.query.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DrugRepository extends Neo4jRepository<Drug, String> {

    Optional<Drug> findByName(String name);

    List<Drug> findByNameContaining(String keyword);

    @Query("MATCH (dr:Drug) RETURN dr ORDER BY dr.name LIMIT 100")
    List<Drug> findAllLimited();

    @Query("MATCH (dr:Drug {name: $name}) DETACH DELETE dr")
    void deleteByName(String name);
}
