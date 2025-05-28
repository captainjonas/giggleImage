package com.giggle.image.model;

import lombok.Data;
import java.util.Map;
import java.util.HashMap;

@Data
public class ComfyUIWorkflow {
    private Map<String, Object> nodes;
    private Map<String, Object> edges;

    public static ComfyUIWorkflow createDefaultWorkflow() {
        ComfyUIWorkflow workflow = new ComfyUIWorkflow();
        workflow.setNodes(new HashMap<>());
        workflow.setEdges(new HashMap<>());
        return workflow;
    }

    public void addNode(String nodeId, Map<String, Object> nodeConfig) {
        nodes.put(nodeId, nodeConfig);
    }

    public void addEdge(String edgeId, Map<String, Object> edgeConfig) {
        edges.put(edgeId, edgeConfig);
    }
} 