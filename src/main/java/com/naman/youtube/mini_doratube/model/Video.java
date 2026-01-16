package com.naman.youtube.mini_doratube.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "videos")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Video {
    @Id
    private UUID id;

    @Column(nullable = false)
    private String title;

    private String description;

    @Column(nullable = false)
    private UUID uploaderID;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private VideoStatus status;

    private Integer duration;
    private Instant createdAt;
}
