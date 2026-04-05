package com.ai.entity.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GraphNodeVO {

    private String id;

    private String name;

    private String type;

    private Object properties;
}
