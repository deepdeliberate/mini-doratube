package com.naman.youtube.mini_doratube.controller;


import com.naman.youtube.mini_doratube.dto.CreateVideoRequest;
import com.naman.youtube.mini_doratube.dto.VideoResponse;
import com.naman.youtube.mini_doratube.service.VideoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("api/videos")
@RequiredArgsConstructor
public class VideoController {
    private final VideoService videoService;

    @PostMapping
    public VideoResponse createVideo(
            @RequestBody @Valid CreateVideoRequest request,
            @RequestHeader("X-USER-ID") UUID userId
    ){
        return videoService.createVideo(request, userId);
    }

    @GetMapping("/{videoId}")
    public VideoResponse getVideo(@PathVariable UUID videoId){
        return videoService.getVideo(videoId);
    }

    @PostMapping("/{videoId}/view")
    public String incrementView(@PathVariable UUID videoId){
        videoService.incrementView(videoId);
        return "View incremented!";
    }

    @GetMapping("/{videoId}/views")
    public long getViews(@PathVariable UUID videoId){
        return videoService.getViewCount(videoId);
    }

    @GetMapping("/{videoId}/upload-url")
    public String getUploadUrl(@PathVariable UUID videoId) throws Exception{
        return videoService.generateUploadUrl(videoId);
    }

    @PostMapping("/{videoId}/complete")
    public void completeUpload(@PathVariable UUID videoId){
        videoService.markUploadComplete(videoId);
    }


}
