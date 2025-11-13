package com.pssa.cineminha.repository;

import com.pssa.cineminha.entity.VideoFile;
import com.pssa.cineminha.entity.VideoStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface VideoRepository extends JpaRepository<VideoFile, UUID> {
    Optional<VideoFile> findBySourcePath(String sourcePath);
    List<VideoFile> findByStatus(VideoStatus status);
}