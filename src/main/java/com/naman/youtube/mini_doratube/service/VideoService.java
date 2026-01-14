package com.naman.youtube.mini_doratube.service;


import com.naman.youtube.mini_doratube.dto.CreateVideoRequest;
import com.naman.youtube.mini_doratube.dto.VideoResponse;
import com.naman.youtube.mini_doratube.model.Video;
import com.naman.youtube.mini_doratube.repository.VideoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import io.minio.GetPresignedObjectUrlArgs;
import io.minio.http.Method;
import io.minio.MinioClient;

import java.time.Instant;
import java.util.Date;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class VideoService {

    private final VideoRepository videoRepository;
    private final RedisTemplate<String, String > redisTemplate;
    private final MinioClient minioClient;

    public VideoResponse createVideo(CreateVideoRequest request, UUID uploaderId) {
        Video video = Video.builder()
                .id(UUID.randomUUID())
                .title(request.getTitle())
                .description(request.getDescription())
                .uploaderID(uploaderId)
                .status("UPLOADING")
                .createdAt(Instant.now())
                .build();

        videoRepository.save(video);

        return VideoResponse.builder()
                .videoId(video.getId())
                .title(video.getTitle())
                .description(video.getTitle())
                .status(video.getStatus())
                .createdAt(video.getCreatedAt())
                .build();
    }

    public VideoResponse getVideo(UUID videoId){
        Video video = videoRepository.findById(videoId)
                .orElseThrow(()-> new RuntimeException("Video not found"));

        return VideoResponse.builder()
                .videoId(video.getId())
                .title(video.getTitle())
                .description(video.getDescription())
                .status(video.getStatus())
                .createdAt(video.getCreatedAt())
                .build();
    }

    public void incrementView(UUID videoId){
        String key = "video:views:" + videoId.toString();
        redisTemplate.opsForValue().increment(key, 1);
    }

    public long getViewCount(UUID videoId){
        String key = "video:views:" + videoId.toString();
        String count = redisTemplate.opsForValue().get(key);
        if(count != null){
            return Long.parseLong(count);
        }
        else{
            return 0;
        }
    }

    public String generateUploadUrl(UUID videoId) throws Exception{
        Date expiry = new Date(System.currentTimeMillis() + 15 * 60 * 1000); // 15mins
        return minioClient.getPresignedObjectUrl(
                GetPresignedObjectUrlArgs.builder()
                        .method(Method.PUT)
                        .bucket("videos")
                        .object(videoId+".mp4")
                        .expiry(15*60)
                        .build()
        );
    }
}
