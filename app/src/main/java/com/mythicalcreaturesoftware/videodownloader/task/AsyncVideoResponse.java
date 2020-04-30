package com.mythicalcreaturesoftware.videodownloader.task;

import java.io.File;

public interface AsyncVideoResponse {
    void videoProcessFinish(File output);
}
