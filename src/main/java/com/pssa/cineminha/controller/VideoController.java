package com.pssa.cineminha.controller;

import com.pssa.cineminha.entity.VideoFile;
import com.pssa.cineminha.entity.VideoStatus;
import com.pssa.cineminha.service.LibraryManagementService;
import com.pssa.cineminha.service.StreamingService;
import org.springframework.core.io.support.ResourceRegion;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/videos")
public class VideoController {

    private LibraryManagementService libraryManagementService;
    private StreamingService streamingService;

    public VideoController(LibraryManagementService libraryManagementService, StreamingService streamingService) {
        this.libraryManagementService = libraryManagementService;
        this.streamingService = streamingService;
    }

    @GetMapping
    public List<VideoFile> getReadyVideos(){
        return this.libraryManagementService.getVideoFilesByStatus(VideoStatus.READY);
    }

    @GetMapping("/stream/{id}")
    public ResourceRegion getVideoStream(
            @PathVariable UUID id,
            @RequestHeader HttpHeaders headers){
        return streamingService.streamVideo(id);
    }

    @GetMapping("/thumbnail/{id}")
    public void getVideoThumbnail(@PathVariable UUID id){
        streamingService.streamThumbnail(id);
    }
}