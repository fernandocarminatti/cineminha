package com.pssa.cineminha.service;

import com.pssa.cineminha.config.MediaStorageProperties;
import com.pssa.cineminha.dto.VideoFileResponseDto;
import com.pssa.cineminha.entity.VideoFile;
import com.pssa.cineminha.repository.VideoRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Stream;

@Service
public class CatalogManagementService {

    private final MediaStorageProperties mediaStorageProperties;
    private final VideoRepository videoRepository;
    private final Logger log = LoggerFactory.getLogger(CatalogManagementService.class);

    public CatalogManagementService(MediaStorageProperties mediaStorageProperties, VideoRepository videoRepository) {
        this.mediaStorageProperties = mediaStorageProperties;
        this.videoRepository = videoRepository;
    }

    public void scanForNewFiles() {
        Path sourcePath = mediaStorageProperties.getSourceDir().normalize();
        log.info("Scan Triggered - Searching in {}", sourcePath);
        if(!Files.exists(sourcePath) || !Files.isDirectory(sourcePath)){
            log.error("Source directory {} does not exist or is not a directory", sourcePath);
            return;
        }

        try (Stream<Path> paths = Files.walk(sourcePath)){
            paths
                    .filter(Files::isRegularFile)
                    .filter(path -> path.toString().endsWith(".mkv"))
                    .forEach(this::processDiscoveredFile);
        } catch (IOException e) {
            log.error("Error scanning for new files", e);
        }
        log.info("Finished library scan");
    }

    public void processDiscoveredFile(Path filePath){
        String fileName = filePath.getFileName().toString();
        Optional<VideoFile> fileAlreadyDiscovered = videoRepository.findBySourceFile(fileName);
        if (fileAlreadyDiscovered.isEmpty()) {
            log.info("New file discovered: {}", filePath.getFileName());
            VideoFile newVideo = new VideoFile(fileName);
            videoRepository.save(newVideo);
            log.info("Saved new video to database: {}", fileName);
        }
    }

    public Optional<VideoFile> getVideoById(UUID id){
        return this.videoRepository.findById(id);
    }

    public List<VideoFile> getAllVideos(){
        return this.videoRepository.findAll();
    }

    public List<VideoFileResponseDto> getStreammableVideos(){
        return this.videoRepository.findAll()
                .stream()
                .map(
                        video -> new VideoFileResponseDto(video.getId(), video.getTitle()))
                .toList();
    }

    public void removeVideoRecord(UUID id){
        try{
            videoRepository.deleteById(id);
        } catch (Exception e){
            log.error("Error deleting video record: {}", id, e);
        }
    }
}