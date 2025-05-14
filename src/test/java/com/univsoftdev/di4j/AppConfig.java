package com.univsoftdev.di4j;

import com.univsoftdev.di4j.annotations.*;

@Component
public class AppConfig {

    @Value("${app.name}")
    private String appName;

    @Value("${max.retries}")
    private int maxRetries;

    @Value("${debug.mode}")
    private boolean debugMode;

    public String getAppName() {
        return appName;
    }

    public void setAppName(String appName) {
        this.appName = appName;
    }

    public int getMaxRetries() {
        return maxRetries;
    }

    public void setMaxRetries(int maxRetries) {
        this.maxRetries = maxRetries;
    }

    public boolean isDebugMode() {
        return debugMode;
    }

    public void setDebugMode(boolean debugMode) {
        this.debugMode = debugMode;
    }

}
