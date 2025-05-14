package com.univsoftdev.di4j;

public class DatabaseConnection {

    private final String url;
    private final String user;

    public DatabaseConnection(String url, String user) {
        this.url = url;
        this.user = user;
    }

    public String getUrl() {
        return url;
    }

    public String getUser() {
        return user;
    }
    
    
}
