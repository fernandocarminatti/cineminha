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
    private final LocalDateTime createdAt = LocalDateTime.now();

    public VideoFile() {}

    public VideoFile(String sourceFile) {
        this.sourceFile = sourceFile;
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

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}