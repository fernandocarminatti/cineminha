package com.pssa.cineminha.controller;

import com.pssa.cineminha.service.CatalogManagementService;
import jakarta.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/admin/catalog")
public class AdminController {

    private final CatalogManagementService catalogManagementService;
    private final Logger log = LoggerFactory.getLogger(AdminController.class);

    public AdminController(CatalogManagementService catalogManagementService) {
        this.catalogManagementService = catalogManagementService;
    }

    @PostMapping("/scan")
    public ResponseEntity<Void> triggerLibraryScan() {
        catalogManagementService.scanForNewFiles();
        log.info("Library scan triggered");
        return ResponseEntity.ok().build();
    }

    @PostMapping("/convert/{id}")
    public ResponseEntity<Resource> triggerVideoConversion(@PathVariable UUID id){
        catalogManagementService.startVideoConversion(id);
        log.info("Video conversion triggered for video with id {}", id);
        return ResponseEntity.ok().build();
    }
}