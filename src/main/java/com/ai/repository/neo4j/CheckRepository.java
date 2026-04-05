package com.ai.repository.neo4j;

import com.ai.entity.neo4j.Check;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.neo4j.repository.query.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CheckRepository extends Neo4jRepository<Check, String> {

    Optional<Check> findByName(String name);

    List<Check> findByNameContaining(String keyword);

    @Query("MATCH (c:Check) RETURN c ORDER BY c.name LIMIT 100")
    List<Check> findAllLimited();
}
