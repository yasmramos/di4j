package com.univsoftdev.di4j;

import com.univsoftdev.di4j.annotations.Component;
import com.univsoftdev.di4j.annotations.PostConstruct;

@Component
class TestService {

    @PostConstruct
    public void init() {
        initialized = true;
    }
    private boolean initialized = false;

    public boolean isInitialized() {
        return initialized;
    }
}
