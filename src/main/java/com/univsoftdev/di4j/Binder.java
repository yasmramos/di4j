package com.univsoftdev.di4j;

import java.util.*;

public class Binder {

    private final Map<Class<?>, Class<?>> bindings = new HashMap<>();

    public <T> void bind(Class<T> interfaceType, Class<? extends T> implementationType) {
        bindings.put(interfaceType, implementationType);
    }

    Map<Class<?>, Class<?>> getBindings() {
        return bindings;
    }
}
