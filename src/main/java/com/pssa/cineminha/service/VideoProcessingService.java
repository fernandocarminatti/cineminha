package com.pssa.cineminha.service;

import com.pssa.cineminha.config.MediaStorageProperties;
import com.pssa.cineminha.entity.VideoFile;
import com.pssa.cineminha.exception.RemuxProcessingException;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Collectors;

@Service
public class VideoProcessingService {

    private final MediaStorageProperties mediaStorageProperties;
    private final Logger log = LoggerFactory.getLogger(VideoProcessingService.class);

    public VideoProcessingService(MediaStorageProperties mediaStorageProperties) {
        this.mediaStorageProperties = mediaStorageProperties;
    }

    @PostConstruct
    public void initDirs() {
        try {
            Files.createDirectories(mediaStorageProperties.getProcessedDir());
            Files.createDirectories(mediaStorageProperties.getThumbnailDir());
        } catch (IOException e) {
            throw new IllegalStateException("Failed to create required media directories", e);
        }
    }

    public void startVideoConversion(VideoFile videoFile) throws IOException, InterruptedException{
        log.info("Starting video conversion for video with id {}", videoFile.getId());
        Path sourceFile = mediaStorageProperties.getSourceDir().normalize().resolve(videoFile.getSourceFile());
        Path mp4OutputPath = mediaStorageProperties.getProcessedDir().normalize().resolve(videoFile.getId() + ".mp4");
        Path thumbnailOutputPath = mediaStorageProperties.getThumbnailDir().normalize().resolve(videoFile.getId() + ".jpg");
        log.info("Starting remuxing of '{}'", videoFile.getTitle());
        String audioStreamIndex = grabEnglishAudio(sourceFile.toString());
        // "Remuxing"
        runProcess(
                "ffmpeg",
                "-i", sourceFile.toString(),
                "-map", "0:v",
                "-map", "0:a:" + audioStreamIndex,
                "-c:v", "copy",
                "-c:a", "aac",
                "-ac", "2",
                "-movflags", "+faststart",
                "-y",
                mp4OutputPath.toString()
        );
        log.info("Successfully remuxed '{}'", videoFile.getTitle());
        log.info("Starting thumbnail creation of '{}'", videoFile.getTitle());
        // Thumbnail
        runProcess(
                "ffmpeg",
                "-i", sourceFile.toString(),
                "-ss", "00:01:00",
                "-vframes", "1",
                "-q:v", "2",
                "-y",
                thumbnailOutputPath.toString()
        );
        videoFile.setProcessedFile(videoFile.getId() + ".mp4");
        videoFile.setThumbnailFile(videoFile.getId() + ".jpg");
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

    public String grabEnglishAudio(String sourcePath) throws IOException, InterruptedException {
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