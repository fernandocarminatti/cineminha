package com.pssa.cineminha.entity;


import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "video_files")
public class VideoFile {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    private String title;
    private String sourcePath;
    private String processedFile;
    private String thumbnailFile;

    @Enumerated(EnumType.STRING)
    private VideoStatus status;
    private final LocalDateTime createdAt = LocalDateTime.now();

    public VideoFile() {}

    public VideoFile(String title, String sourcePath) {
        this.title = title;
        this.sourcePath = sourcePath;
        this.status = VideoStatus.NEW;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getSourcePath() {
        return sourcePath;
    }

    public void setSourcePath(String sourcePath) {
        this.sourcePath = sourcePath;
    }

    public String getProcessedFile() {
        return processedFile;
    }

    public void setProcessedFile(String processedFile) {
        this.processedFile = processedFile;
    }

    public String getThumbnailFile() {
        return thumbnailFile;
    }

    public void setThumbnailFile(String thumbnailFile) {
        this.thumbnailFile = thumbnailFile;
    }

    public VideoStatus getStatus() {
        return status;
    }

    public void setStatus(VideoStatus status) {
        this.status = status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

}