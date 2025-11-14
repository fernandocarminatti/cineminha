package com.pssa.cineminha.controller;

import com.pssa.cineminha.entity.VideoFile;
import com.pssa.cineminha.service.CatalogManagementService;
import jakarta.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/admin/catalog")
public class AdminController {

    private final CatalogManagementService catalogManagementService;
    private final Logger log = LoggerFactory.getLogger(AdminController.class);

    public AdminController(CatalogManagementService catalogManagementService) {
        this.catalogManagementService = catalogManagementService;
    }

    @GetMapping
    public ResponseEntity<List<VideoFile>> getAllVideos(){
        List<VideoFile> videos = this.catalogManagementService.getAllVideos();
        return ResponseEntity.ok(videos);
    }

    @GetMapping("{id}")
    public ResponseEntity<VideoFile> getVideoById(@PathVariable UUID id){
        Optional<VideoFile> video = this.catalogManagementService.getVideoById(id);
        return video.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping("/scan")
    public ResponseEntity<Void> triggerLibraryScan() {
        catalogManagementService.scanForNewFiles();
        return ResponseEntity.ok().build();
    }

    @PostMapping("/convert/{id}")
    public ResponseEntity<Resource> triggerVideoConversion(@PathVariable UUID id){
        catalogManagementService.startVideoConversion(id);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteVideo(@PathVariable UUID id){
        boolean deleted = catalogManagementService.deleteVideoRecord(id);
        if (deleted) {
            return ResponseEntity.ok().build();
        }
        return ResponseEntity.notFound().build();
    }
}