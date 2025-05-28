package com.giggle.image.controller;

import com.giggle.image.dto.ImageGenerationRequest;
import com.giggle.image.dto.TaskResponse;
import com.giggle.image.service.ComfyUIService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class ImageGenerationController {

    private final ComfyUIService comfyUIService;

    @PostMapping("/generate")
    public ResponseEntity<TaskResponse> generateImage(@Valid @RequestBody ImageGenerationRequest request) {
        log.info("Received image generation request: {}", request);
        TaskResponse response = comfyUIService.generateImage(request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/tasks/{taskId}")
    public ResponseEntity<TaskResponse> getTaskStatus(@PathVariable String taskId) {
        log.info("Getting status for task: {}", taskId);
        TaskResponse response = comfyUIService.getTaskStatus(taskId);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/tasks/{taskId}")
    public ResponseEntity<Void> cancelTask(@PathVariable String taskId) {
        log.info("Cancelling task: {}", taskId);
        comfyUIService.cancelTask(taskId);
        return ResponseEntity.noContent().build();
    }
} 