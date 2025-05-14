package com.univsoftdev.di4j;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

public abstract class AbstractModule implements Module{
    
    private Binder binder;
    
    private final Map<Class<?>, Class<?>> bindings = new HashMap<>();
    private final Map<Class<?>, Object> instances = new HashMap<>();
    private final Map<Class<?>, Supplier<?>> providers = new HashMap<>();
    private final List<InterceptorBinding> interceptors = new ArrayList<>();

    
    /**
     * Binds an interface or base class to a concrete implementation.
     */
    protected <T> void bind(Class<T> baseType, Class<? extends T> implementation) {
        bindings.put(baseType, implementation);
    }

    /**
     * Binds a specific instance to a type.
     */
    protected <T> void bindInstance(Class<T> baseType, T instance) {
        instances.put(baseType, instance);
    }

    /**
     * Binds a provider (Supplier) for dynamic instance creation.
     */
    protected <T> void bindProvider(Class<T> baseType, Supplier<T> provider) {
        providers.put(baseType, provider);
    }

    /**
     * Binds an interceptor to methods matching a given pattern.
     */
    protected void bindInterceptor(Matcher<Class<?>> classMatcher, Matcher<Method> methodMatcher, MethodInterceptor... interceptors) {
        this.interceptors.add(new InterceptorBinding(classMatcher, methodMatcher, interceptors));
    }

    /**
     * Retrieves all bindings.
     */
    public Map<Class<?>, Class<?>> getBindings() {
        return bindings;
    }

    /**
     * Retrieves all instances.
     */
    public Map<Class<?>, Object> getInstances() {
        return instances;
    }

    /**
     * Retrieves all providers.
     */
    public Map<Class<?>, Supplier<?>> getProviders() {
        return providers;
    }

    /**
     * Retrieves all interceptors.
     */
    public List<InterceptorBinding> getInterceptors() {
        return interceptors;
    }

    /**
     * Abstract method for configuration.
     */
    protected abstract void configure();
}
