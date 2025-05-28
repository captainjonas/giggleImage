package com.giggle.image.service;

import com.giggle.image.service.TaskQueueService;
import com.giggle.image.dto.TaskResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class ComfyUIResultPoller {

    @Value("${comfyui.server.url}")
    private String comfyUIServerUrl;

    private final RestTemplate restTemplate;
    private final TaskQueueService taskQueueService;

    @Async
    public void pollResult(String taskId, String promptId) {
        String historyUrl = comfyUIServerUrl + "/history/" + promptId;
        int maxAttempts = 60; // 5 minutes with 5-second intervals
        int attempts = 0;

        while (attempts < maxAttempts) {
            try {
                Map<String, Object> history = restTemplate.getForObject(historyUrl, Map.class);
                if (history != null && history.containsKey(promptId)) {
                    Map<String, Object> result = (Map<String, Object>) history.get(promptId);
                    if (result.containsKey("outputs")) {
                        Map<String, Object> outputs = (Map<String, Object>) result.get("outputs");
                        if (outputs.containsKey("output")) {
                            Map<String, Object> output = (Map<String, Object>) outputs.get("output");
                            if (output.containsKey("images")) {
                                // Success - update task with image URL
                                String imageUrl = comfyUIServerUrl + "/view?filename=" + 
                                    ((Map<String, Object>) ((java.util.List<?>) output.get("images")).get(0)).get("filename");
                                updateTaskSuccess(taskId, imageUrl);
                                return;
                            }
                        }
                    }
                }
                
                TimeUnit.SECONDS.sleep(5);
                attempts++;
            } catch (Exception e) {
                log.error("Error polling result for task {}: {}", taskId, e.getMessage());
                updateTaskError(taskId, "Error polling result: " + e.getMessage());
                return;
            }
        }

        updateTaskError(taskId, "Timeout waiting for result");
    }

    private void updateTaskSuccess(String taskId, String imageUrl) {
        TaskResponse task = taskQueueService.getTaskStatus(taskId);
        if (task != null) {
            task.setStatus("COMPLETED");
            task.setResultUrl(imageUrl);
            task.setUpdatedAt(System.currentTimeMillis());
            taskQueueService.updateTaskStatus(taskId, task);
            log.info("Task {} completed successfully", taskId);
        }
    }

    private void updateTaskError(String taskId, String error) {
        TaskResponse task = taskQueueService.getTaskStatus(taskId);
        if (task != null) {
            task.setStatus("FAILED");
            task.setError(error);
            task.setUpdatedAt(System.currentTimeMillis());
            taskQueueService.updateTaskStatus(taskId, task);
            log.error("Task {} failed: {}", taskId, error);
        }
    }
}