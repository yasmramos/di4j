package com.univsoftdev.di4j;

public class Configuration {

    private boolean lazyInit = false;
    private boolean autoDetectComponents = true;
    private String[] basePackages = {};

    public Configuration setLazyInit(boolean lazyInit) {
        this.lazyInit = lazyInit;
        return this;
    }

    public Configuration setAutoDetectComponents(boolean autoDetectComponents) {
        this.autoDetectComponents = autoDetectComponents;
        return this;
    }

    public Configuration setBasePackages(String... basePackages) {
        this.basePackages = basePackages;
        return this;
    }

    // Getters
    public boolean isLazyInit() {
        return lazyInit;
    }

    public boolean isAutoDetectComponents() {
        return autoDetectComponents;
    }

    public String[] getBasePackages() {
        return basePackages;
    }
}
