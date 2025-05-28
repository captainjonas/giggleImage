package com.giggle.image.dto;

import lombok.Data;
import lombok.Builder;

@Data
@Builder
public class TaskResponse {
    private String taskId;
    private String status;
    private String resultUrl;
    private String error;
    private Long createdAt;
    private Long updatedAt;
} 