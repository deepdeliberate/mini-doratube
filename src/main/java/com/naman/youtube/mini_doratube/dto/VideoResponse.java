package com.naman.youtube.mini_doratube.dto;

import lombok.Builder;
import lombok.Data;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

@Data
@Builder
public class VideoResponse {
    private UUID videoId;
    private String title;
    private String description;
    private String status;
    private Instant createdAt;
    private Map<String, String> streamURLs;
    private String masterStreamUrl;

}
