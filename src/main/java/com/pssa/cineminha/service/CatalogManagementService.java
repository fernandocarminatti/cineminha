package com.pssa.cineminha.service;

import com.pssa.cineminha.config.MediaStorageProperties;
import com.pssa.cineminha.dto.VideoFileResponseDto;
import com.pssa.cineminha.entity.VideoFile;
import com.pssa.cineminha.entity.VideoStatus;
import com.pssa.cineminha.exception.VideoNotFoundException;
import com.pssa.cineminha.repository.VideoRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
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
    private final VideoProcessingService videoProcessingService;
    private final Logger log = LoggerFactory.getLogger(CatalogManagementService.class);

    public CatalogManagementService(MediaStorageProperties mediaStorageProperties, VideoRepository videoRepository, VideoProcessingService videoProcessingService) {
        this.mediaStorageProperties = mediaStorageProperties;
        this.videoRepository = videoRepository;
        this.videoProcessingService = videoProcessingService;
    }

    public void scanForNewFiles() {
        Path sourcePath = mediaStorageProperties.getSourceDir().normalize();
        log.info("Scan Triggered - Searching in {}", sourcePath);

        if(!Files.exists(sourcePath) || !Files.isDirectory(sourcePath)){
            log.error("Source directory {} does not exist or is not a directory", sourcePath);
            return;
        }

        try (Stream<Path> paths = Files.walk(sourcePath )){
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

    @Async
    public void triggerVideoProcessing(UUID videoId) {
        VideoFile video = videoRepository.findById(videoId).orElseThrow(VideoNotFoundException::new);
        video.setStatus(VideoStatus.CONVERTING);
        videoRepository.save(video);
        try {
            videoProcessingService.startVideoConversion(video);
            video.setStatus(VideoStatus.READY);
            videoRepository.save(video);
        } catch(IOException e){
            log.error("IO Error processing video with id {}", videoId, e);
            video.setStatus(VideoStatus.ERROR);
            videoRepository.save(video);
        } catch(InterruptedException e){
            log.error("Interrupted while processing video with id {}", videoId, e);
            video.setStatus(VideoStatus.ERROR);
            videoRepository.save(video);
            Thread.currentThread().interrupt();
        }
    }

    public List<VideoFileResponseDto> getVideoFilesByStatus(VideoStatus status){
        return this.videoRepository.findByStatus(status);
    }

    public Optional<VideoFile> getVideoById(UUID id){
        return this.videoRepository.findById(id);
    }

    public List<VideoFile> getAllVideos(){
        return this.videoRepository.findAll();
    }

    public void deleteVideoRecord(UUID id){
        Optional<VideoFile> video = this.videoRepository.findById(id);
        if(video.isEmpty()){
            log.info("Video with id {} not found - Nothing to do.", id);
            throw new VideoNotFoundException();
        }

        if(video.get().getProcessedFile() == null || video.get().getThumbnailFile() == null){
            log.info("Video with id {} has no processed path or thumbnail file - Nothing to do.", id);
            throw new VideoNotFoundException();
        }

        try {
            log.info("Trying to remove record {} from disk", id);
            removeFromDisk(video.get());
            videoRepository.deleteById(id);
        } catch (Exception e) {
            log.error("Error deleting video record with id {}", id, e);
        }
    }

    public void removeFromDisk(VideoFile video) {
        try{
            Files.delete(mediaStorageProperties.getProcessedDir().normalize().resolve(video.getProcessedFile()));
            Files.delete(mediaStorageProperties.getThumbnailDir().normalize().resolve(video.getThumbnailFile()));
            log.info("Successfully deleted video file from disk");
        } catch (IOException e){
            log.error("Error deleting video file from disk: {}", e.getMessage());
        }
    }
}