package com.example.adiuvo;

public class MediaItem {
    private String path; // File path of the media
    private boolean isVideo; // True if it's a video, false if it's an image

    public MediaItem(String path, boolean isVideo) {
        this.path = path;
        this.isVideo = isVideo;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public boolean isVideo() {
        return path.toLowerCase().endsWith(".mp4") || path.toLowerCase().endsWith(".3gp");
    }

    public void setVideo(boolean video) {
        isVideo = video;
    }
// Constructor, getters, and setters go here...
}

