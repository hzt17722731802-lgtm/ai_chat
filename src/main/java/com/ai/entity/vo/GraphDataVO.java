package com.ai.entity.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GraphDataVO {

    private List<GraphNodeVO> nodes;

    private List<GraphLinkVO> links;
}
