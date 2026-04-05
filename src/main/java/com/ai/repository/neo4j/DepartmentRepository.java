package com.ai.repository.neo4j;

import com.ai.entity.neo4j.Department;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.neo4j.repository.query.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DepartmentRepository extends Neo4jRepository<Department, String> {

    Optional<Department> findByName(String name);

    List<Department> findByNameContaining(String keyword);

    @Query("MATCH (dep:Department) RETURN dep ORDER BY dep.name")
    List<Department> findAllDepartments();
}
