package com.pssa.cineminha.controller;

import com.pssa.cineminha.dto.VideoFileResponseDto;
import com.pssa.cineminha.entity.VideoStatus;
import com.pssa.cineminha.service.CatalogManagementService;
import com.pssa.cineminha.service.StreamingService;
import org.springframework.core.io.support.ResourceRegion;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/videos")
public class VideoController {

    private final CatalogManagementService catalogManagementService;
    private final StreamingService streamingService;

    public VideoController(CatalogManagementService catalogManagementService, StreamingService streamingService) {
        this.catalogManagementService = catalogManagementService;
        this.streamingService = streamingService;
    }

    @GetMapping
    public ResponseEntity<List<VideoFileResponseDto>> getReadyVideos(){
        List<VideoFileResponseDto> readyVideos = this.catalogManagementService.getVideoFilesByStatus(VideoStatus.READY);
        return ResponseEntity.ok(readyVideos);
    }

    @GetMapping("/stream/{id}")
    public ResponseEntity<ResourceRegion> getVideoStream(
            @PathVariable UUID id,
            @RequestHeader HttpHeaders headers){
        return streamingService.streamVideo(id, headers);
    }
}