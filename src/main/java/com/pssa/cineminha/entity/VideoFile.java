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
    private String sourceFile;
    private String processedFile;
    private String thumbnailFile;

    @Enumerated(EnumType.STRING)
    private VideoStatus status;
    private final LocalDateTime createdAt = LocalDateTime.now();

    public VideoFile() {}

    public VideoFile(String sourceFile) {
        this.title = sourceFile.substring(0, sourceFile.lastIndexOf('.'))
                .replaceAll("[._]", " ");
        this.sourceFile = sourceFile;
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

    public String getSourceFile() {
        return sourceFile;
    }

    public void setSourceFile(String sourceFile) {
        this.sourceFile = sourceFile;
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
        if(status == VideoStatus.ERROR){
            this.clearFiles();
        }
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void clearFiles(){
        this.processedFile = null;
        this.thumbnailFile = null;
    }
}