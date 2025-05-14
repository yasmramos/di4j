package com.univsoftdev.di4j;

import java.util.function.Supplier;

public class BeanDefinition {

    private final Class<?> type;
    private final String qualifier;
    private final boolean singleton;
    private final boolean primary;
    private boolean lazy;
    private Supplier<?> supplier;

    public BeanDefinition(Class<?> type, String qualifier, boolean isSingleton, boolean primary) {
        this.type = type;
        this.qualifier = qualifier;
        this.singleton = isSingleton;
        this.primary = primary;
    }

    public BeanDefinition(Class<?> type, String qualifier, boolean singleton, Supplier<?> instanceSupplier) {
        this.type = type;
        this.qualifier = qualifier;
        this.singleton = singleton;
        this.supplier = instanceSupplier;
        this.primary = false;
    }

    public Supplier<?> getSupplier() {
        return supplier;
    }

    public void setSupplier(Supplier<?> supplier) {
        this.supplier = supplier;
    }

    public boolean isLazy() {
        return lazy;
    }

    public void setLazy(boolean lazy) {
        this.lazy = lazy;
    }

    public boolean isPrimary() {
        return primary;
    }

    public Class<?> getType() {
        return type;
    }

    public String getQualifier() {
        return qualifier;
    }

    public boolean isSingleton() {
        return singleton;
    }
}
