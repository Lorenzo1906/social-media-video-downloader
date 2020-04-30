package com.mythicalcreaturesoftware.videodownloader.model;

import java.net.URI;
import java.net.URISyntaxException;

public class ImageData {
    private String filename;
    private String url;
    private String userImageUrl;
    private String username;
    private String videoUrl;
    private boolean isVideo;

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        try {
            this.filename = getUrlWithoutParameters(filename);
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }

        if (this.filename.equals("")){
            int random = (int)(Math.random() * 50 + 1);
            String prefix = "image";
            if (isVideo) {
                prefix = "video";
            }

            this.filename = prefix + random;
        }
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public boolean isVideo() {
        return isVideo;
    }

    public void setVideo(boolean video) {
        isVideo = video;
    }

    public String getUserImageUrl() {
        return userImageUrl;
    }

    public void setUserImageUrl(String userImageUrl) {
        this.userImageUrl = userImageUrl;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getVideoUrl() {
        return videoUrl;
    }

    public void setVideoUrl(String videoUrl) {
        this.videoUrl = videoUrl;
    }

    private String getUrlWithoutParameters(String url) throws URISyntaxException {
        URI uri = new URI(url);
        return new URI(uri.getScheme(),
                uri.getAuthority(),
                uri.getPath(),
                null, // Ignore the query part of the input url
                uri.getFragment()).toString();
    }
}
