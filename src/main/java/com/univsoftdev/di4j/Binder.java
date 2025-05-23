package com.univsoftdev.di4j;

import java.util.*;
import java.util.function.Supplier;

public class Binder {

    private final Map<Class<?>, Class<?>> bindings = new HashMap<>();
    private final Map<Class<?>, Object> instances = new HashMap<>();
    private final Map<Class<?>, Supplier<?>> providers = new HashMap<>();

    public <T> void bind(Class<T> interfaceType, Class<? extends T> implementationType) {
        bindings.put(interfaceType, implementationType);
    }

    public <T> void bindInstance(Class<T> type, T instance) {
        instances.put(type, instance);
    }

    public <T> void bindProvider(Class<T> type, Supplier<? extends T> provider) {
        providers.put(type, provider);
    }

    Map<Class<?>, Class<?>> getBindings() {
        return bindings;
    }

    public Map<Class<?>, Object> getInstances() {
        return instances;
    }

    public Map<Class<?>, Supplier<?>> getProviders() {
        return providers;
    }

    public void install(Module module) {
        module.configure(this); // 'this' refers to the current Binder instance
    }
}
