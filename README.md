# Giggle Image Generation Service

A microservice for text-to-image generation using ComfyUI and StoryDiffusion.

## Prerequisites

- Docker and Docker Compose
- NVIDIA GPU with CUDA support
- NVIDIA Container Toolkit installed

## Setup

1. Clone the repository:
```bash
git clone https://github.com/your-org/giggle-image.git
cd giggle-image
```

2. Create the models directory:
```bash
mkdir -p comfyui_models
```

3. Download the required models and place them in the `comfyui_models` directory:
   - Stable Diffusion model
   - StoryDiffusion models

4. Start the services:
```bash
docker-compose up -d
```

## API Endpoints

### Generate Image
```http
POST /api/v1/generate
Content-Type: application/json

{
    "prompt": "your prompt here",
    "negativePrompt": "optional negative prompt",
    "width": 512,
    "height": 512,
    "steps": 20,
    "cfgScale": 7.0,
    "sampler": "euler_a",
    "seed": -1
}
```

### Get Task Status
```http
GET /api/v1/tasks/{taskId}
```

### Cancel Task
```http
DELETE /api/v1/tasks/{taskId}
```

## Development

### Building the Project
```bash
./mvnw clean install
```

### Running Tests
```bash
./mvnw test
```

## Monitoring

The service includes Spring Boot Actuator endpoints for monitoring:
- Health check: `/actuator/health`
- Metrics: `/actuator/metrics`
- Prometheus metrics: `/actuator/prometheus`

## License

[Your License Here]
