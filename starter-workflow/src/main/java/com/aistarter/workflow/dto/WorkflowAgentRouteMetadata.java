package com.aistarter.workflow.dto;

import com.aistarter.rag.dto.RagSource;
import lombok.Data;

import java.util.List;

@Data
public class WorkflowAgentRouteMetadata {

    private List<RagSource> sources;
}
