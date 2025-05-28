package com.giggle.image.service.impl;

import com.giggle.image.dto.ImageGenerationRequest;
import com.giggle.image.dto.TaskResponse;
import com.giggle.image.model.ComfyUIWorkflow;
import com.giggle.image.service.ComfyUIService;
import com.giggle.image.service.TaskQueueService;
import com.giggle.image.service.ComfyUIResultPoller;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import java.time.Instant;

@Slf4j
@Service
@RequiredArgsConstructor
public class ComfyUIServiceImpl implements ComfyUIService {

    @Value("${comfyui.server.url}")
    private String comfyUIServerUrl;

    private final RestTemplate restTemplate;
    private final TaskQueueService taskQueueService;
    private final ComfyUIResultPoller resultPoller;

    @Override
    public TaskResponse generateImage(ImageGenerationRequest request) {
        String taskId = UUID.randomUUID().toString();
        log.info("Starting image generation task: {}", taskId);

        TaskResponse task = TaskResponse.builder()
                .taskId(taskId)
                .status("PENDING")
                .createdAt(Instant.now().toEpochMilli())
                .updatedAt(Instant.now().toEpochMilli())
                .build();

        taskQueueService.enqueueTask(taskId, task);
        
        // Create and execute workflow asynchronously
        ComfyUIWorkflow workflow = createWorkflow(request);
        executeWorkflow(taskId, workflow);

        return task;
    }

    @Override
    public TaskResponse getTaskStatus(String taskId) {
        TaskResponse task = taskQueueService.getTaskStatus(taskId);
        if (task == null) {
            return TaskResponse.builder()
                    .taskId(taskId)
                    .status("NOT_FOUND")
                    .updatedAt(Instant.now().toEpochMilli())
                    .build();
        }
        return task;
    }

    @Override
    public void cancelTask(String taskId) {
        log.info("Cancelling task: {}", taskId);
        String cancelUrl = comfyUIServerUrl + "/interrupt";
        restTemplate.postForObject(cancelUrl, null, Void.class);
        taskQueueService.removeTask(taskId);
    }

    private ComfyUIWorkflow createWorkflow(ImageGenerationRequest request) {
        ComfyUIWorkflow workflow = ComfyUIWorkflow.createDefaultWorkflow();
        
        // Add StoryDiffusion nodes
        Map<String, Object> storyNode = new HashMap<>();
        storyNode.put("class_type", "StoryDiffusion");
        storyNode.put("inputs", Map.of(
            "prompt", request.getPrompt(),
            "negative_prompt", request.getNegativePrompt(),
            "width", request.getWidth(),
            "height", request.getHeight(),
            "steps", request.getSteps(),
            "cfg", request.getCfgScale(),
            "sampler_name", request.getSampler(),
            "seed", request.getSeed()
        ));
        workflow.addNode("story_diffusion", storyNode);

        // Add output node
        Map<String, Object> outputNode = new HashMap<>();
        outputNode.put("class_type", "SaveImage");
        outputNode.put("inputs", Map.of(
            "filename_prefix", "ComfyUI",
            "images", List.of("story_diffusion", 0)
        ));
        workflow.addNode("output", outputNode);

        // Add edge
        workflow.addEdge("edge1", Map.of(
            "from_node", "story_diffusion",
            "from_socket", 0,
            "to_node", "output",
            "to_socket", 0
        ));

        return workflow;
    }

    private void executeWorkflow(String taskId, ComfyUIWorkflow workflow) {
        String queueUrl = comfyUIServerUrl + "/prompt";
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<ComfyUIWorkflow> request = new HttpEntity<>(workflow, headers);
        Map<String, Object> response = restTemplate.postForObject(queueUrl, request, Map.class);

        if (response != null && response.containsKey("prompt_id")) {
            String promptId = (String) response.get("prompt_id");
            log.info("Workflow queued with prompt_id: {}", promptId);
            
            // Start polling for results
            startResultPolling(taskId, promptId);
        } else {
            log.error("Failed to queue workflow for task: {}", taskId);
            updateTaskStatus(taskId, "FAILED", "Failed to queue workflow");
        }
    }

    private void startResultPolling(String taskId, String promptId) {
        resultPoller.pollResult(taskId, promptId);
    }

    private void updateTaskStatus(String taskId, String status, String error) {
        TaskResponse task = taskQueueService.getTaskStatus(taskId);
        if (task != null) {
            task.setStatus(status);
            task.setError(error);
            task.setUpdatedAt(Instant.now().toEpochMilli());
            taskQueueService.updateTaskStatus(taskId, task);
        }
    }
} 