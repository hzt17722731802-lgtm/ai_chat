package com.ai.entity.neo4j;

import lombok.Data;
import org.springframework.data.neo4j.core.schema.GeneratedValue;
import org.springframework.data.neo4j.core.schema.Id;
import org.springframework.data.neo4j.core.schema.Node;
import org.springframework.data.neo4j.core.schema.Property;

import java.util.List;

@Data
@Node("Disease")
public class Disease {

    @Id
    @GeneratedValue
    private String id;

    @Property("name")
    private String name;

    @Property("desc")
    private String desc;

    @Property("cause")
    private String cause;

    @Property("prevent")
    private String prevent;

    @Property("easy_get")
    private String easyGet;

    @Property("cure_lasttime")
    private String cureLasttime;

    @Property("cured_prob")
    private String curedProb;

    @Property("cure_department")
    private List<String> cureDepartment;

    @Property("cure_way")
    private List<String> cureWay;

    public Disease() {}
}
