package com.giggle.image.service;

import com.giggle.image.dto.TaskResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class TaskQueueService {
    private static final String TASK_QUEUE_KEY = "image:generation:tasks";
    private static final String TASK_STATUS_KEY_PREFIX = "image:generation:status:";
    private static final long TASK_TIMEOUT_HOURS = 24;

    private final RedisTemplate<String, Object> redisTemplate;

    public void enqueueTask(String taskId, TaskResponse task) {
        String statusKey = TASK_STATUS_KEY_PREFIX + taskId;
        redisTemplate.opsForValue().set(statusKey, task, TASK_TIMEOUT_HOURS, TimeUnit.HOURS);
        redisTemplate.opsForList().rightPush(TASK_QUEUE_KEY, taskId);
        log.info("Task {} enqueued", taskId);
    }

    public TaskResponse getTaskStatus(String taskId) {
        String statusKey = TASK_STATUS_KEY_PREFIX + taskId;
        TaskResponse task = (TaskResponse) redisTemplate.opsForValue().get(statusKey);
        if (task == null) {
            log.warn("Task {} not found", taskId);
            return null;
        }
        return task;
    }

    public void updateTaskStatus(String taskId, TaskResponse task) {
        String statusKey = TASK_STATUS_KEY_PREFIX + taskId;
        redisTemplate.opsForValue().set(statusKey, task, TASK_TIMEOUT_HOURS, TimeUnit.HOURS);
        log.info("Task {} status updated to {}", taskId, task.getStatus());
    }

    public String dequeueTask() {
        return (String) redisTemplate.opsForList().leftPop(TASK_QUEUE_KEY);
    }

    public void removeTask(String taskId) {
        String statusKey = TASK_STATUS_KEY_PREFIX + taskId;
        redisTemplate.delete(statusKey);
        redisTemplate.opsForList().remove(TASK_QUEUE_KEY, 0, taskId);
        log.info("Task {} removed", taskId);
    }
} 