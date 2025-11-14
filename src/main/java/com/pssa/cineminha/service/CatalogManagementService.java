package com.pssa.cineminha.service;

import com.pssa.cineminha.dto.VideoFileResponseDto;
import com.pssa.cineminha.entity.VideoFile;
import com.pssa.cineminha.entity.VideoStatus;
import com.pssa.cineminha.exception.RemuxProcessingException;
import com.pssa.cineminha.repository.VideoRepository;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
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

    @PostConstruct
    public void initDirs() {
        try {
            Files.createDirectories(Paths.get(processedDir));
            Files.createDirectories(Paths.get(thumbnailDir));
        } catch (IOException e) {
            throw new IllegalStateException("Failed to create required media directories", e);
        }
    }

    public void scanForNewFiles() {
        log.info("Scan Triggered - Searching in {}", sourceDir);
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
            Path mp4OutputPath = Paths.get(processedDir, video.getId() + ".mp4");
            Path thumbnailOutputPath = Paths.get(thumbnailDir, video.getId() + ".jpg");


            log.info("Starting remuxing of '{}'", video.getTitle());
            String audioStreamIndex = grabEnglishAudio(sourcePath.toString());
            // "Remuxing"
            runProcess(
                    "ffmpeg",
                    "-i", sourcePath.toString(),
                    "-map", "0:v",
                    "-map", "0:a:" + audioStreamIndex,
                    "-c:v", "copy",
                    "-c:a", "aac",
                    "-ac", "2",
                    "-movflags", "+faststart",
                    "-y",
                    mp4OutputPath.toString()
            );
            log.info("Successfully remuxed '{}'", video.getTitle());
            log.info("Starting thumbnail creation of '{}'", video.getTitle());
            // Thumbnail
            runProcess(
                    "ffmpeg",
                    "-i", sourcePath.toString(),
                    "-ss", "00:01:00",
                    "-vframes", "1",
                    "-q:v", "2",
                    "-y",
                    thumbnailOutputPath.toString()
            );

            video.setProcessedFile(mp4OutputPath.getFileName().toString());
            video.setThumbnailFile(thumbnailOutputPath.getFileName().toString());
            video.setStatus(VideoStatus.READY);
            videoRepository.save(video);
            log.info("Finished runprocess '{}'", video.getTitle());
        } catch (IOException | InterruptedException e) {
            video.setStatus(VideoStatus.ERROR);
            videoRepository.save(video);
            log.error("IOException for '{}'. Error: {}", video.getTitle(), e.getMessage());
        }
    }

    private void runProcess(String... command) throws IOException, InterruptedException {
        log.info("Executing command: {}", String.join(" ", command));
        ProcessBuilder processBuilder = new ProcessBuilder(command);
        processBuilder.inheritIO();
        Process process = processBuilder.start();
        int exitCode = process.waitFor();
        if (exitCode != 0) {
            throw new RemuxProcessingException(exitCode + " returned from remuxing process");
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

    public boolean deleteVideoRecord(UUID id){
        Optional<VideoFile> video = this.videoRepository.findById(id);
        if(video.isEmpty()){
            log.info("Video with id {} not found - Nothing to do.", id);
            return false;
        }

        if(video.get().getProcessedFile() == null || video.get().getThumbnailFile() == null){
            log.info("Video with id {} has no processed path or thumbnail file - Nothing to do.", id);
            return false;
        }

        try {
            log.info("Trying to remove record {} from disk", id);
            removeFromDisk(video.get());
            videoRepository.deleteById(id);
            return true;
        } catch (Exception e) {
            log.error("Error deleting video record with id {}", id, e);
            return false;
        }
    }

    public void removeFromDisk(VideoFile video) {
        try{
            Files.delete(Paths.get(video.getProcessedFile()));
            Files.delete(Paths.get(video.getThumbnailFile()));
            log.info("Successfully deleted video file from disk");
        } catch (IOException e){
            log.error("Error deleting video file from disk: {}", e.getMessage());
        }
    }

    public String grabEnglishAudio(String sourcePath) throws IOException, InterruptedException {
        // TODO: Exchange into audio/subtitle generation via request DTO if usage of this idea happens.
        ProcessBuilder pb = new ProcessBuilder(
                "ffprobe",
                "-v", "error",
                "-select_streams", "a",
                "-show_entries", "stream=index:stream_tags=language",
                "-of", "csv=p=0",
                sourcePath
        );
        pb.redirectErrorStream(true);
        Process process = pb.start();
        String output;
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
            output = reader.lines().collect(Collectors.joining("\n"));
        }
        int exitCode = process.waitFor();
        if (exitCode != 0) {
            throw new RemuxProcessingException("ffprobe returned " + exitCode);
        }
        log.info("ffprobe finished. ExitCode - {}", exitCode);
        String[] lines = output.split("\\R");
        for (int i = 0; i < lines.length; i++) {
            if (lines[i].contains("eng")) {
                log.info("Found eng audio stream index {}", i);
                return String.valueOf(i);
            }
        }
        log.info("No eng audio stream found. Fallback into index 0");
        return "0";
    }
}