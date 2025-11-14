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

This project is intended to be run as a self-contained Docker image. You can build and run it locally using Docker.

```bash
# Build the Docker image
docker build -t cineminha:0.0.1 .
```

### Configuration

Create a `.env` file with the required environment variables:

```bash
ADMIN_USERNAME=your_admin_user
ADMIN_PASSWORD=your_strong_password
H2_USERNAME=h2_database_username
H2_PASSWORD=your_h2_password
```

The application will fail to start if these environment variables are not provided.

| Environment Variable | Description                                 |
|----------------------|---------------------------------------------|
| `ADMIN_USERNAME`     | The username for the administrator account. |
| `ADMIN_PASSWORD`     | The password for the administrator account. |
| `H2_USERNAME`        | The username for the database account.      |
| `H2_PASSWORD`        | The password for the database account.      |

### Running the Application

Run the application using Docker with your environment file and media volume:

```bash
docker run -d \
  --env-file ~/.dev-config/cineminha.env \
  -v ~/path/to/source/mkv:/app/media/source:ro \
  -p 8080:8080 \
  cineminha:0.0.1
```

This command will:
- Run the container in detached mode (`-d`)
- Load environment variables from your config file
- Mount your media source directory as read-only. `-v ~/host:/container:ro`
- Expose the application on port 8080 or whatever port you prefer.

### License

This is free software. You can do whatever you want with it.

It was built for fun, using tools others made for free. In that same spirit, you are free to copy it, change it, sell it, or print out the source code and use it to line your hamster cage. **I DO NOT CARE**. No warranty is provided, express or implied.