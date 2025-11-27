package com.pssa.cineminha.service;

import com.pssa.cineminha.config.MediaStorageProperties;
import com.pssa.cineminha.entity.VideoFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.ResourceRegion;
import org.springframework.http.*;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Optional;
import java.util.UUID;

@Service
public class StreamingService {

    private static final int CHUNK_SIZE = 1024 * 1024;
    private final MediaStorageProperties mediaStorageProperties;
    private final CatalogManagementService catalogManagementService;
    private final Logger log = LoggerFactory.getLogger(StreamingService.class);

    public StreamingService(MediaStorageProperties mediaStorageProperties, CatalogManagementService catalogManagementService) {
        this.mediaStorageProperties = mediaStorageProperties;
        this.catalogManagementService = catalogManagementService;
    }

    public ResponseEntity<ResourceRegion> streamVideo(UUID videoId, HttpHeaders headers) {
        Optional<VideoFile> optionalVideo = catalogManagementService.getVideoById(videoId);
        if (optionalVideo.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        VideoFile video = optionalVideo.get();
        try {
            Path videoPath = mediaStorageProperties.getSourceDir().normalize().resolve(video.getSourceFile());
            Resource resource = new FileSystemResource(videoPath);
            log.info("Trying to serve from disk - {} ", resource.getFilename());
            long length = resource.contentLength();
            HttpRange range = headers.getRange().stream().findFirst().orElse(null);

            if (range != null) {
                log.info("Range requested: {}", range);
                long start = range.getRangeStart(length);
                long end = range.getRangeEnd(length);
                long contentLength = end - start + 1;
                ResourceRegion region = new ResourceRegion(resource, start, contentLength);
                return ResponseEntity.status(HttpStatus.PARTIAL_CONTENT)
                        .contentType(MediaType.valueOf("video/x-matroska"))
                        .contentLength(contentLength)
                        .body(region);
            } else {
                log.info("No range requested");
                ResourceRegion region = new ResourceRegion(resource, 0, Math.min(CHUNK_SIZE, length));
                return ResponseEntity.status(HttpStatus.PARTIAL_CONTENT)
                        .contentType(MediaType.valueOf("video/x-matroska"))
                        .contentLength(region.getCount())
                        .body(region);
            }
        } catch (IOException e) {
            log.error("Error streaming video with ID {}: {}", videoId, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}