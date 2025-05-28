package com.giggle.image.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ImageGenerationRequest {
    @NotBlank(message = "Prompt cannot be empty")
    private String prompt;
    
    private String negativePrompt;
    private Integer width = 512;
    private Integer height = 512;
    private Integer steps = 20;
    private Float cfgScale = 7.0f;
    private String sampler = "euler_a";
    private Long seed = -1L;
} 