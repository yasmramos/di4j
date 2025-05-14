package com.univsoftdev.di4j;

class CustomService {

    private final String value;

    public CustomService(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
