package com.naman.youtube.mini_doratube.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CreateVideoRequest {
    @NotBlank
    private String title;
    private String description;
}
