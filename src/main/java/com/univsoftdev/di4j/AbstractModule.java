package com.univsoftdev.di4j;

import java.util.function.Supplier;

public abstract class AbstractModule implements Module {
    protected Binder binder;

    @Override
    public final void configure(Binder binder) { // Made final to ensure binder is set
        this.binder = binder;
        configureBindings();
    }

    /**
     * Subclasses must implement this method to define their bindings.
     * They can use the protected `bind`, `bindInstance`, `bindProvider` methods.
     */
    protected abstract void configureBindings(); // Subclasses define bindings here

    protected <T> void bind(Class<T> baseType, Class<? extends T> implementation) {
        if (this.binder == null) throw new IllegalStateException("Binder not initialized. Ensure configure() was called.");
        this.binder.bind(baseType, implementation);
    }

    protected <T> void bindInstance(Class<T> baseType, T instance) {
        if (this.binder == null) throw new IllegalStateException("Binder not initialized.");
        this.binder.bindInstance(baseType, instance);
    }

    protected <T> void bindProvider(Class<T> baseType, Supplier<T> provider) {
        if (this.binder == null) throw new IllegalStateException("Binder not initialized.");
        this.binder.bindProvider(baseType, provider);
    }

    // bindInterceptor and its getter are removed for now.

    protected void install(Module module) {
        if (this.binder == null) {
            throw new IllegalStateException("Binder not initialized. Ensure configure() was called before installing modules.");
        }
        this.binder.install(module);
    }
}
