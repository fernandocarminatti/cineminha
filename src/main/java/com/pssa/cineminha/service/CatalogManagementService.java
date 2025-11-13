package com.pssa.cineminha.service;

import com.pssa.cineminha.dto.VideoFileResponseDto;
import com.pssa.cineminha.entity.VideoFile;
import com.pssa.cineminha.entity.VideoStatus;
import com.pssa.cineminha.exception.RemuxProcessingException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import com.pssa.cineminha.repository.VideoRepository;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Stream;

@Service
public class CatalogManagementService {
    @Value("${app.storage.source-dir}")
    private String sourceDir;
    @Value("${app.storage.processed-dir}")
    private String processedDir;
    @Value("${app.storage.thumbnail-dir}")
    private String thumbnailDir;

    private final VideoRepository videoRepository;
    private final Logger log = LoggerFactory.getLogger(CatalogManagementService.class);

    public CatalogManagementService(VideoRepository videoRepository) {
        this.videoRepository = videoRepository;
    }

    public void scanForNewFiles() {
        log.info("Scanning for new files in {}", sourceDir);
        Path sourcePath = Paths.get(sourceDir);

        if(!Files.exists(sourcePath) || !Files.isDirectory(sourcePath)){
            log.error("Source directory {} does not exist or is not a directory", sourceDir);
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
        String absolutePath = filePath.toAbsolutePath().toString();
        Optional<VideoFile> fileAlreadyDiscovered = videoRepository.findBySourcePath(absolutePath);
        if (fileAlreadyDiscovered.isEmpty()) {
            log.info("New file discovered: {}", filePath.getFileName());
            String fileName = filePath.getFileName().toString();
            String title = fileName.substring(0, fileName.lastIndexOf('.'))
                    .replaceAll("[._]", " ");
            VideoFile newVideo = new VideoFile(title, absolutePath);
            videoRepository.save(newVideo);
            log.info("Saved new video to database with title: {}", title);
        }
    }

    @Async
    public void startVideoConversion(UUID videoId){
        Optional<VideoFile> optVideoFile = videoRepository.findById(videoId);
        if (optVideoFile.isEmpty()) {
            log.error("Video with id {} not found", videoId);
            return;
        }
        VideoFile video = optVideoFile.get();
        optVideoFile.get().setStatus(VideoStatus.CONVERTING);
        videoRepository.save(video);
        log.info("Starting video conversion for video with id {}", videoId);

        try{
            Path sourcePath = Paths.get(video.getSourcePath());
            String baseFileName = sourcePath.getFileName().toString().replaceFirst("[.][^.]+$","");

            Path mp4OutputPath = Paths.get(processedDir, baseFileName + ".mp4");
            Path thumbnailOutputPath = Paths.get(thumbnailDir, baseFileName + ".jpg");

            new File(processedDir).mkdirs();
            new File(thumbnailDir).mkdirs();

            // "Remuxing"
            runProcess(
                    "ffmpeg",
                    "-i", sourcePath.toString(),
                    "-c:v", "copy",
                    "-c:a", "aac",
                    "-ac", "2",
                    "-movflags", "+faststart",
                    "-y",
                    mp4OutputPath.toString()
            );
            log.info("Successfully remuxed '{}'", video.getTitle());
            // Thumbnail
            runProcess(
                    "ffmpeg",
                    "-i", mp4OutputPath.toString(),
                    "-ss", "00:01:00",
                    "-vframes", "1",
                    "-q:v", "2",
                    "-y",
                    thumbnailOutputPath.toString()
            );

            video.setProcessedPath(mp4OutputPath.toString());
            video.setThumbnailPath(thumbnailOutputPath.toString());
            video.setStatus(VideoStatus.READY);
            videoRepository.save(video);
            log.info("Finished runprocess '{}'", video.getTitle());
        } catch (Exception e) {
            video.setStatus(VideoStatus.ERROR);
            videoRepository.save(video);
            log.error("Failed to convert '{}'. Error: {}", video.getTitle(), e.getMessage());
        }
    }

    private void runProcess(String... command) throws Exception {
        log.info("Executing command: {}", String.join(" ", command));
        ProcessBuilder processBuilder = new ProcessBuilder(command);
        processBuilder.inheritIO();
        Process process = processBuilder.start();
        int exitCode = process.waitFor();
        if (exitCode != 0) {
            throw new RemuxProcessingException("Error on remuxing file " + exitCode);
        }
    }

    public List<VideoFileResponseDto> getVideoFilesByStatus(VideoStatus status){
        return this.videoRepository.findByStatus(status);
    }

    public Optional<VideoFile> getVideoById(UUID id){
        return this.videoRepository.findById(id);
    }
}