package com.ai.entity.neo4j;

import lombok.Data;
import org.springframework.data.neo4j.core.schema.GeneratedValue;
import org.springframework.data.neo4j.core.schema.Id;
import org.springframework.data.neo4j.core.schema.Node;
import org.springframework.data.neo4j.core.schema.Property;

@Data
@Node("Drug")
public class Drug {

    @Id
    @GeneratedValue
    private String id;

    @Property("name")
    private String name;

    public Drug() {}

    public Drug(String name) {
        this.name = name;
    }
}
