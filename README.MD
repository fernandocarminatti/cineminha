# cineminha

A simple, self-hosted, personal video streaming application.

### Core Concept

This project was created to explore the feasibility of a private, centralized video streaming service. The main idea is to eliminate the common "download, watch, re-upload" cycle for sharing media among a small, private group.

The application is designed to ingest high-quality media files (e.g., `.mkv`) from a source directory and process them into a universally streamable format (`.mp4`) suitable for web browsers.

### Features

*   Backend API built with Spring Boot.
*   Simple, session-based authentication using Spring Security.
*   Media ingestion pipeline that scans a source directory for new files.
*   Asynchronous, background video processing using **FFmpeg** to:
    *   Remux video streams without quality loss.
    *   Downmix surround sound audio to web-compatible stereo AAC.
    *   Generate thumbnails.
*   Efficient video streaming with HTTP Range Request support for seeking and buffering.
*   Basic admin API for managing the library (scanning and converting).

### Tech Stack

This project is built with the following technologies:

*   **Backend:** Java (17+), Spring Boot
    *   **Spring Web:** For REST APIs.
    *   **Spring Data JPA:** For database interaction.
    *   **Spring Security:** For authentication and endpoint protection.
*   **Database:** H2 (File-based, persistent).
*   **Build Tool:** Maven
*   **Runtime Dependency:** **FFmpeg** is required for all video and audio processing.

### Getting Started

This project is built with the idea of eventually being served as a self-contained Docker image. However, a public image is not yet available.

For now, you are on your own to run it. The application can be started directly from the source using the Maven wrapper:

```bash
# Set the required environment variables first
export ADMIN_USERNAME="your_admin_user"
export ADMIN_PASSWORD="your_strong_password"
export H2_USERNAME="h2_database_username"
export H2_PASSWORD=""

# Run the application
./mvnw spring-boot:run
```

### Configuration

The application is configured via environment variables to set the administrator credentials. The application will fail to start if these are not provided.

| Environment Variable | Description                                 |
|----------------------|---------------------------------------------|
| `ADMIN_USERNAME`     | The username for the administrator account. |
| `ADMIN_PASSWORD`     | The password for the administrator account. |
| `H2_USERNAME`        | The username for the database account.      |
| `H2_PASSWORD`        | The password for the database account.      |

### License

This is free software. You can do whatever you want with it.

It was built for fun, using tools others made for free. In that same spirit, you are free to copy it, change it, sell it, or print out the source code and use it to line your hamster cage. **I DO NOT CARE**. No warranty is provided, express or implied.