package com.naman.youtube.mini_doratube.queue;


import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class VideoProcessingQueue {
    private final RedisTemplate<String, String> redisTemplate;
    private static final String QUEUE_KEY = "video:processing:queue";

    public void publish(UUID videoId){
        redisTemplate.opsForList().leftPush(QUEUE_KEY, videoId.toString());
    }
}
