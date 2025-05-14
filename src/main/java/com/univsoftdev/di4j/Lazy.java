package com.univsoftdev.di4j;

public class Lazy<T> {

    private final Injector injector;
    private final Class<T> type;
    private volatile T instance;
    private final String qualifier;

    public Lazy(Injector injector, Class<T> type) {
        this.injector = injector;
        this.type = type;
        this.qualifier = null;
    }

    public Lazy(Injector injector, Class<T> type, String qualifier) {
        this.injector = injector;
        this.type = type;
        this.qualifier = qualifier;
    }

    public T get() {
        if (instance == null) {
            synchronized (this) {
                if (instance == null) {
                    instance = (qualifier != null)
                            ? injector.resolveQualified(type, qualifier)
                            : injector.resolve(type);
                }
            }
        }
        return instance;
    }
}
