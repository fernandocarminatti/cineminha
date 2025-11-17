package com.pssa.cineminha.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.nio.file.Path;

@Component
@ConfigurationProperties(prefix = "app.storage")
public class MediaStorageProperties {

    /**
     * Base directory where original source files are located.
     */
    private Path sourceDir;

    /**
     * Base directory where processed MP4 files are stored.
     */
    private Path processedDir;

    /**
     * Base directory where thumbnail images are stored.
     */
    private Path thumbnailDir;

    public Path getSourceDir() {
        return sourceDir;
    }

    public void setSourceDir(Path sourceDir) {
        this.sourceDir = sourceDir;
    }

    public Path getProcessedDir() {
        return processedDir;
    }

    public void setProcessedDir(Path processedDir) {
        this.processedDir = processedDir;
    }

    public Path getThumbnailDir() {
        return thumbnailDir;
    }

    public void setThumbnailDir(Path thumbnailDir) {
        this.thumbnailDir = thumbnailDir;
    }
}