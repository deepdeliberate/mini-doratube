package com.naman.youtube.mini_doratube.service;


import com.naman.youtube.mini_doratube.dto.CreateVideoRequest;
import com.naman.youtube.mini_doratube.dto.VideoResponse;
import com.naman.youtube.mini_doratube.model.Video;
import com.naman.youtube.mini_doratube.repository.VideoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class VideoService {

    private final VideoRepository videoRepository;

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
}
