package com.giggle.image.service;

import com.giggle.image.dto.ImageGenerationRequest;
import com.giggle.image.dto.TaskResponse;

public interface ComfyUIService {
    TaskResponse generateImage(ImageGenerationRequest request);
    TaskResponse getTaskStatus(String taskId);
    void cancelTask(String taskId);
} 