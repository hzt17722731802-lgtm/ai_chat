package com.ai.entity.neo4j;

import lombok.Data;
import org.springframework.data.neo4j.core.schema.GeneratedValue;
import org.springframework.data.neo4j.core.schema.Id;
import org.springframework.data.neo4j.core.schema.Node;
import org.springframework.data.neo4j.core.schema.Property;

@Data
@Node("Check")
public class Check {

    @Id
    @GeneratedValue
    private String id;

    @Property("name")
    private String name;

    public Check() {}

    public Check(String name) {
        this.name = name;
    }
}
