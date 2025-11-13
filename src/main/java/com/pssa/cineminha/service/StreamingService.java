package com.pssa.cineminha.service;

import org.springframework.core.io.support.ResourceRegion;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class StreamingService {

    public ResourceRegion streamVideo(UUID videoId) {
        return null;
    }

    public void streamThumbnail(UUID videoId) {
    }
}