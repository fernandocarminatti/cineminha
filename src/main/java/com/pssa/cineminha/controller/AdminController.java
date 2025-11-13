package com.pssa.cineminha.controller;

import com.pssa.cineminha.service.LibraryManagementService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/admin/library")
public class AdminController {

    public LibraryManagementService libraryManagementService;
    private final Logger log = LoggerFactory.getLogger(AdminController.class);

    public AdminController(LibraryManagementService libraryManagementService) {
        this.libraryManagementService = libraryManagementService;
    }

    @PostMapping("/scan")
    public ResponseEntity<Void> triggerLibraryScan() {
        libraryManagementService.scanForNewFiles();
        log.info("Library scan triggered");
        return ResponseEntity.ok().build();
    }

    @PostMapping("/convert/{id}")
    public ResponseEntity<Void> triggerVideoConversion(@PathVariable UUID id){
        libraryManagementService.startVideoConversion(id);
        log.info("Video conversion triggered for video with id {}", id);
        return ResponseEntity.ok().build();
    }
}