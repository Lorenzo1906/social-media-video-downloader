package com.mythicalcreaturesoftware.videodownloader.task;

import java.util.Map;

public interface AsyncResponse {
    void processFinish(Map<String, Object> output);
}
