package com.naman.youtube.mini_doratube.repository;

import com.naman.youtube.mini_doratube.model.Video;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface VideoRepository extends JpaRepository<Video, UUID> {
}
