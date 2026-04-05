package com.ai.entity.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GraphLinkVO {

    private String source;

    private String target;

    private String type;

    private String sourceName;

    private String targetName;
}
