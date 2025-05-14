package com.univsoftdev.di4j;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import com.univsoftdev.di4j.annotations.Component;
import com.univsoftdev.di4j.annotations.Inject;
import com.univsoftdev.di4j.annotations.PostConstruct;
import com.univsoftdev.di4j.annotations.PreDestroy;
import com.univsoftdev.di4j.annotations.Primary;
import com.univsoftdev.di4j.annotations.Qualifier;
import com.univsoftdev.di4j.annotations.Scope;
import com.univsoftdev.di4j.annotations.Value;
import com.univsoftdev.di4j.exceptions.BeanCreationException;
import com.univsoftdev.di4j.exceptions.BeanResolutionException;
import com.univsoftdev.di4j.exceptions.BeanValidationException;

/**
 * The Injector class is responsible for managing dependency injection. It
 * maintains bean definitions, singleton instances, and initialization tracking.
 */
public class Injector {

    private static final Logger LOGGER = Logger.getLogger(Injector.class.getName());
    /**
     * A thread-local stack used to track bean resolution to prevent circular
     * dependencies.
     */
    private final ThreadLocal<Deque<BeanKey>> resolutionStack = ThreadLocal.withInitial(ArrayDeque::new);

    /**
     * A map storing bean definitions, indexed by their keys.
     */
    private final Map<BeanKey, BeanDefinition> beanDefinitions = new HashMap<>();

    /**
     * A map storing singleton instances of beans.
     */
    private final Map<BeanKey, Object> singletons = new ConcurrentHashMap<>();

    /**
     * A set tracking initialized beans to avoid redundant initialization.
     */
    private final Set<Object> initializedBeans = new HashSet<>();

    /**
     * A set of component classes managed by the injector.
     */
    private final Set<Class<?>> componentClasses;

    /**
     * Configuration settings for the injector.
     */
    private final Configuration configuration;

    /**
     * A map storing lists of bean definitions for interface implementations.
     */
    private final Map<Class<?>, List<BeanDefinition>> interfaceImplementations = new HashMap<>();

    /**
     * A list of post-processors applied to beans after initialization.
     */
    private final List<BeanPostProcessor> postProcessors = new ArrayList<>();

    /**
     * Properties used for configuration and bean initialization.
     */
    private Properties properties = new Properties();

    /**
     * A map storing bindings from modules.
     */
    private final Map<Class<?>, Class<?>> bindings = new HashMap<>();

    /**
     * A map storing instances bound explicitly.
     */
    private final Map<Class<?>, Object> instances = new ConcurrentHashMap<>();

    /**
     * A map storing providers for dynamic instance creation.
     */
    private final Map<Class<?>, Supplier<?>> providers = new HashMap<>();

    /**
     * Default constructor initializing the injector with a default
     * configuration.
     */
    public Injector() {
        this(new Configuration());
    }

    /**
     * Constructs an Injector with the specified configuration. It scans for
     * components, registers them, validates beans, and initializes non-lazy
     * singletons.
     *
     * @param config The configuration settings for the injector.
     */
    public Injector(Configuration config) {
        this.configuration = config;
        this.componentClasses = new HashSet<>();

        // Scan for components if auto-detection is enabled
        if (configuration.isAutoDetectComponents()) {
            for (String basePackage : configuration.getBasePackages()) {
                componentClasses.addAll(ClassScanner.scanForComponents(basePackage));
            }
        }

        // Register all detected components without creating instances
        for (Class<?> componentClass : componentClasses) {
            try {
                registerComponent(componentClass);
            } catch (Exception e) {
                LOGGER.log(Level.SEVERE, "Failed to register component: " + componentClass.getName(), e);
            }
        }

        // Validate bean definitions to ensure correctness
        try {
            validateBeans();
        } catch (BeanValidationException ex) {
            Logger.getLogger(Injector.class.getName()).log(Level.SEVERE, null, ex);
        }

        // Initialize singleton beans that are not marked as lazy
        initializeNonLazySingletons();
    }

    /**
     * Constructs an Injector with the specified configuration and loads
     * properties from a file.
     *
     * @param config The configuration settings for the injector.
     * @param propertiesFile The name of the properties file to load.
     * @throws RuntimeException If an error occurs while loading the properties
     * file.
     */
    public Injector(Configuration config, String propertiesFile) {
        this(config);
        try (InputStream input = getClass().getClassLoader().getResourceAsStream(propertiesFile)) {
            if (input == null) {
                throw new FileNotFoundException("Property file '" + propertiesFile + "' not found");
            }
            properties.load(input);
        } catch (IOException e) {
            throw new BeanResolutionException("Error loading properties from external source", e);
        }
    }

    public void loadProperties(ConfigurationSource source) {
        try {
            properties.putAll(source.load());
        } catch (IOException e) {
            throw new BeanResolutionException("Error loading properties from external source", e);
        }
    }

    public Injector(AbstractModule module) {
        this.configuration = new Configuration();
        this.componentClasses = new HashSet<>();
        module.configure();
        bindings.putAll(module.getBindings());
        instances.putAll(module.getInstances());
        providers.putAll(module.getProviders());
    }

    /**
     * Resolves an instance of the specified type.
     *
     * @param <T> The type of the instance to resolve.
     * @param type The class type to resolve.
     * @return An instance of the resolved type.
     * @throws BeanResolutionException If no binding is found.
     */
    public <T> T getInstance(Class<T> type) {
        // Check if an instance is already bound
        if (instances.containsKey(type)) {
            return type.cast(instances.get(type));
        }

        // Check if a provider exists
        if (providers.containsKey(type)) {
            return type.cast(providers.get(type).get());
        }

        // Check for a class binding
        Class<?> implementation = bindings.get(type);
        if (implementation == null) {
            throw new BeanResolutionException("No binding found for " + type.getName());
        }

        try {
            T instance = type.cast(implementation.getDeclaredConstructor().newInstance());
            instances.put(type, instance);
            return instance;
        } catch (IllegalAccessException | IllegalArgumentException | InstantiationException | NoSuchMethodException | SecurityException | InvocationTargetException e) {
            throw new BeanResolutionException("Error creating instance of " + type.getName(), e);
        }
    }

    /**
     * Creates an Injector instance with the default configuration.
     *
     * @return A new Injector instance.
     */
    public static Injector createInjector() {
        return new Injector(new Configuration());
    }

    /**
     * Creates an Injector instance with the specified configuration.
     *
     * @param configuration The configuration settings for the injector.
     * @return A new Injector instance.
     */
    public static Injector createInjector(Configuration configuration) {
        return new Injector(configuration);
    }

    /**
     * Registers a component class by creating a bean definition and storing it.
     * It also associates the class with its implemented interfaces.
     *
     * @param <T> The type of the component.
     * @param type The class type of the component to register.
     */
    private <T> void registerComponent(Class<T> type) {
        String qualifier = determineQualifier(type);
        boolean isSingleton = isSingleton(type);
        BeanKey key = new BeanKey(type, qualifier);
        boolean isPrimary = type.isAnnotationPresent(Primary.class);
        boolean isLazy = type.isAnnotationPresent(com.univsoftdev.di4j.annotations.Lazy.class) || configuration.isLazyInit();

        // Register the bean definition if it hasn't been registered yet
        if (!beanDefinitions.containsKey(key)) {
            BeanDefinition definition = new BeanDefinition(type, qualifier, isSingleton, isPrimary);
            definition.setLazy(isLazy);
            beanDefinitions.put(key, definition);

            // Register the component as an implementation of its interfaces
            for (Class<?> interfaceType : type.getInterfaces()) {
                interfaceImplementations.computeIfAbsent(interfaceType, k -> new ArrayList<>())
                        .add(definition);
            }
        }
    }

    /**
     * Registers a supplier for a given component type. The singleton status is
     * determined automatically.
     *
     * @param <T> The type of the component.
     * @param type The class type of the component.
     * @param supplier The supplier function to provide instances of the
     * component.
     */
    public <T> void registerSupplier(Class<T> type, Supplier<T> supplier) {
        registerSupplier(type, supplier, isSingleton(type));
    }

    /**
     * Registers a supplier for a given component type with explicit singleton
     * control.
     *
     * @param <T> The type of the component.
     * @param type The class type of the component.
     * @param supplier The supplier function to provide instances of the
     * component.
     * @param isSingleton Whether the component should be treated as a
     * singleton.
     */
    public <T> void registerSupplier(Class<T> type, Supplier<T> supplier, boolean isSingleton) {
        String qualifier = determineQualifier(type);
        BeanKey key = new BeanKey(type, qualifier);

        // Prevent overwriting existing beans
        if (!beanDefinitions.containsKey(key)) {
            BeanDefinition definition = new BeanDefinition(
                    type,
                    qualifier,
                    isSingleton,
                    type.isAnnotationPresent(Primary.class)
            );
            definition.setLazy(type.isAnnotationPresent(com.univsoftdev.di4j.annotations.Lazy.class) || configuration.isLazyInit());
            definition.setSupplier(supplier);
            beanDefinitions.put(key, definition);

            // Immediately create and store singleton if not lazy
            if (isSingleton && !definition.isLazy()) {
                createAndStoreSingleton(key, definition);
            }
        }
    }

    /**
     * Adds a post-processor to modify beans after initialization.
     *
     * @param postProcessor The bean post-processor to add.
     */
    public void addPostProcessor(BeanPostProcessor postProcessor) {
        postProcessors.add(postProcessor);
    }

    /**
     * Resolves a property value and converts it to the appropriate type based
     * on the field.
     *
     * @param field The field requiring a value injection.
     * @param key The property key to look up.
     * @return The resolved and converted value.
     * @throws RuntimeException If the property is not found or the type is
     * unsupported.
     */
    private Object resolveValue(Field field, String key) {
        String value = properties.getProperty(key);
        if (value == null) {
            throw new RuntimeException("Property not found: " + key);
        }

        Class<?> type = field.getType();
        if (type == String.class) {
            return value;
        }
        if (type == int.class || type == Integer.class) {
            return Integer.valueOf(value);
        }
        if (type == boolean.class || type == Boolean.class) {
            return Boolean.valueOf(value);
        }
        if (type == long.class || type == Long.class) {
            return Long.valueOf(value);
        }
        if (type == double.class || type == Double.class) {
            return Double.valueOf(value);
        }
        throw new RuntimeException("Unsupported type for @Value: " + type.getName());
    }

    /**
     * Registers a class as a component if it is annotated with @Component.
     *
     * @param <T> The type of the component.
     * @param type The class type to register.
     * @throws IllegalArgumentException If the class is not annotated with
     * @Component.
     */
    public <T> void register(Class<T> type) {
        Component component = type.getAnnotation(Component.class);
        if (component == null) {
            throw new IllegalArgumentException("Class must be annotated with @Component: " + type.getName());
        }
        registerComponent(type);
    }

    /**
     * Creates an instance of the specified class, resolving dependencies and
     * injecting fields. It also detects circular dependencies and prevents
     * infinite loops.
     *
     * @param <T> The type of the instance to create.
     * @param type The class type to instantiate.
     * @return A fully initialized instance of the specified class.
     * @throws RuntimeException If a circular dependency is detected or instance
     * creation fails.
     */
    private <T> T createInstance(Class<T> type) {
        String qualifier = determineQualifier(type);
        BeanKey key = new BeanKey(type, qualifier);
        Deque<BeanKey> stack = resolutionStack.get();

        // Check for circular dependencies before proceeding
        if (stack.contains(key)) {
            List<BeanKey> pathList = new ArrayList<>(stack);
            Collections.reverse(pathList); // Reverse order for better readability
            pathList.add(key); // Add the current element at the end
            String path = pathList.stream()
                    .map(BeanKey::toString)
                    .collect(Collectors.joining(" -> "));
            throw new BeanResolutionException("Circular dependency detected:\n" + path);
        }

        stack.push(key);
        try {
            BeanDefinition definition = beanDefinitions.get(key);

            // If a supplier is registered, use it to create the instance
            if (definition != null && definition.getSupplier() != null) {
                return (T) definition.getSupplier().get();
            }

            // Normal instantiation using constructor
            Constructor<T> constructor = findInjectableConstructor(type);
            if (constructor == null) {
                constructor = type.getDeclaredConstructor();
            }
            constructor.setAccessible(true);

            Object[] parameters = getConstructorParameters(constructor);
            T instance = constructor.newInstance(parameters);

            // Inject fields and initialize the bean
            injectFields(instance);
            initializeBean(instance, type.getSimpleName());

            return instance;

        } catch (IllegalAccessException | IllegalArgumentException | InstantiationException
                | NoSuchMethodException | SecurityException | InvocationTargetException e) {
            throw new BeanResolutionException("Error creating instance of " + type.getName(), e);
        } catch (BeanCreationException ex) {
            Logger.getLogger(Injector.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            stack.pop();
            // Clean up the stack if empty to reuse the thread
            if (stack.isEmpty()) {
                resolutionStack.remove();
            }
        }
        return null;
    }

    /**
     * Finds a constructor annotated with @Inject in the given class.
     *
     * @param <T> The type of the class.
     * @param type The class to inspect.
     * @return The injectable constructor, or null if none is found.
     */
    private <T> Constructor<T> findInjectableConstructor(Class<T> type) {
        List<Constructor<?>> constructors = Arrays.asList(type.getDeclaredConstructors());

        // Priorizar constructor con @Inject
        Optional<Constructor<?>> injectConstructor = constructors.stream()
                .filter(c -> c.isAnnotationPresent(Inject.class))
                .findFirst();

        if (injectConstructor.isPresent()) {
            return (Constructor<T>) injectConstructor.get();
        }

        // Si no hay @Inject, elegir el constructor con más parámetros
        return (Constructor<T>) constructors.stream()
                .max(Comparator.comparingInt(Constructor::getParameterCount))
                .orElseThrow(() -> new BeanResolutionException("No suitable constructor found for " + type.getName()));
    }

    /**
     * Resolves the parameters required for a constructor, handling special
     * cases like Lazy<T> and Supplier<T>.
     *
     * @param constructor The constructor whose parameters need to be resolved.
     * @return An array of resolved parameter values.
     */
    private Object[] getConstructorParameters(Constructor<?> constructor) {
        return Arrays.stream(constructor.getParameters())
                .map(param -> {
                    // Handle Lazy<T>
                    if (param.getType().equals(Lazy.class)) {
                        Type genericType = param.getParameterizedType();
                        if (genericType instanceof ParameterizedType parameterizedType) {
                            Type[] typeArgs = parameterizedType.getActualTypeArguments();
                            Class<?> targetType = (Class<?>) typeArgs[0];
                            return new Lazy<>(this, targetType);
                        }
                        throw new RuntimeException("Lazy must have a generic type");
                    }

                    // Handle Supplier<T>
                    if (param.getType().equals(Supplier.class)) {
                        Type genericType = param.getParameterizedType();
                        if (genericType instanceof ParameterizedType parameterizedType) {
                            Type[] typeArgs = parameterizedType.getActualTypeArguments();
                            Class<?> targetType = (Class<?>) typeArgs[0];
                            return (Supplier<?>) () -> resolve(targetType);
                        }
                    }

                    // Handle normal parameter resolution
                    Qualifier qualifier = param.getAnnotation(Qualifier.class);
                    return (qualifier != null)
                            ? resolveQualified(param.getType(), qualifier.value())
                            : resolve(param.getType());
                })
                .toArray();
    }

    /**
     * Injects dependencies into fields annotated with @Inject or @Value.
     *
     * @param instance The instance whose fields need to be injected.
     */
    private void injectFields(Object instance) throws BeanCreationException {
        for (Field field : instance.getClass().getDeclaredFields()) {
            if (field.isAnnotationPresent(Inject.class) || field.isAnnotationPresent(Value.class)) {
                injectField(instance, field);
            }
        }

        for (Method method : instance.getClass().getDeclaredMethods()) {
            if (method.isAnnotationPresent(Inject.class) || method.isAnnotationPresent(Value.class)) {
                injectMethod(instance, method);
            }
        }
    }

    private void injectMethod(Object instance, Method method) {
        try {
            method.setAccessible(true);
            Parameter[] parameters = method.getParameters();
            Object[] args = new Object[parameters.length];

            for (int i = 0; i < parameters.length; i++) {
                Parameter param = parameters[i];
                args[i] = resolveParameter(param);
            }

            method.invoke(instance, args);
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException("Error injecting method: " + method.getName(), e);
        }
    }

    private Object resolveParameter(Parameter param) {
        if (param.isAnnotationPresent(Value.class)) {
            Value value = param.getAnnotation(Value.class);
            String key = value.value().replace("${", "").replace("}", "");
            return resolveParameterValue(param, key);
        }
        if (param.isAnnotationPresent(Inject.class)) {
            Qualifier qualifier = param.getAnnotation(Qualifier.class);
            return qualifier != null
                    ? resolveQualified(param.getType(), qualifier.value())
                    : resolve(param.getType());
        }
        throw new IllegalStateException("Parameter without injection annotation: " + param.getName());
    }

    private Object resolveParameterValue(Parameter param, String key) {
        Class<?> type = param.getType();
        String value = properties.getProperty(key);

        if (value == null) {
            throw new RuntimeException("Property not found: " + key);
        }

        if (type == String.class) {
            return value;
        }
        if (type == int.class || type == Integer.class) {
            return Integer.valueOf(value);
        }
        if (type == boolean.class || type == Boolean.class) {
            return Boolean.valueOf(value);
        }
        if (type == long.class || type == Long.class) {
            return Long.valueOf(value);
        }
        if (type == double.class || type == Double.class) {
            return Double.valueOf(value);
        }

        throw new RuntimeException("Unsupported type for @Value: " + type.getName());
    }

    /**
     * Injects a specific field with the appropriate value or dependency.
     *
     * @param instance The instance containing the field.
     * @param field The field to inject.
     * @throws RuntimeException If an error occurs during injection.
     */
    private void injectField(Object instance, Field field) throws BeanCreationException {
        try {
            field.setAccessible(true); // Esto permite acceder a campos privados

            if (field.isAnnotationPresent(Value.class)) {
                Value valueAnnotation = field.getAnnotation(Value.class);
                String key = valueAnnotation.value().replace("${", "").replace("}", "");
                Object value = resolveValue(field, key);
                field.set(instance, value);
                return;
            }

            if (field.isAnnotationPresent(Inject.class)) {
                Class<?> fieldType = field.getType();

                if (fieldType.equals(Lazy.class)) {
                    field.set(instance, createLazyForField(field));
                    return;
                }

                Qualifier qualifier = field.getAnnotation(Qualifier.class);
                Object value = qualifier != null
                        ? resolveQualified(fieldType, qualifier.value())
                        : resolve(fieldType);

                field.set(instance, value);
            }
        } catch (IllegalAccessException e) {
            throw new RuntimeException("Error injecting field: " + field.getName(), e);
        }
    }

    /**
     * Resolves an instance of a concrete class, creating and registering a bean
     * definition if necessary.
     *
     * @param <T> The type of the class to resolve.
     * @param implementationClass The concrete class to instantiate.
     * @return An instance of the resolved class.
     */
    private <T> T resolveByClass(Class<T> implementationClass) {
        // Look for an existing bean definition or create a new one
        String qualifier = determineQualifier(implementationClass);
        BeanKey key = new BeanKey(implementationClass, qualifier);

        BeanDefinition definition = beanDefinitions.get(key);
        if (definition == null) {
            // If no definition exists, create and register one
            boolean isSingleton = isSingleton(implementationClass);
            boolean isPrimary = implementationClass.isAnnotationPresent(Primary.class);
            definition = new BeanDefinition(implementationClass, qualifier, isSingleton, isPrimary);
            beanDefinitions.put(key, definition);

            if (isSingleton) {
                createAndStoreSingleton(key, definition);
            }
        }

        return resolveDefinition(key, definition);
    }

    /**
     * Resolves an instance of the specified type, handling both interfaces and
     * concrete classes.
     *
     * @param <T> The type of the instance to resolve.
     * @param type The class or interface type to resolve.
     * @return An instance of the resolved type.
     * @throws RuntimeException If no bean is found or multiple primary beans
     * exist for an interface.
     */
    public <T> T resolve(Class<T> type) {
        if (type.isInterface()) {
            List<BeanDefinition> candidates = interfaceImplementations.getOrDefault(type, Collections.emptyList());
            if (candidates.isEmpty()) {
                throw new RuntimeException("No bean found for interface: " + type.getName());
            }

            List<BeanDefinition> primaryCandidates = candidates.stream()
                    .filter(BeanDefinition::isPrimary)
                    .collect(Collectors.toList());

            if (!primaryCandidates.isEmpty()) {
                if (primaryCandidates.size() > 1) {
                    throw new RuntimeException("Multiple primary beans found for: " + type.getName());
                }
                BeanDefinition primary = primaryCandidates.get(0);
                return resolveDefinition(new BeanKey(primary.getType(), primary.getQualifier()), primary);
            }

            // If no primary bean exists, throw an error if multiple implementations are found
            if (candidates.size() > 1) {
                throw new RuntimeException("Multiple implementations found for " + type.getName()
                        + ". Use @Qualifier or mark one as @Primary.");
            }

            BeanDefinition definition = candidates.get(0);
            return resolveDefinition(new BeanKey(definition.getType(), definition.getQualifier()), definition);
        }

        // For concrete classes, resolve by type and default qualifier
        String defaultQualifier = determineQualifier(type);
        BeanKey key = new BeanKey(type, defaultQualifier);
        BeanDefinition definition = beanDefinitions.get(key);

        if (definition == null) {
            throw new RuntimeException("No bean found for type: " + type.getName() + " with qualifier: " + defaultQualifier);
        }
        return resolveDefinition(key, definition);
    }

    /**
     * Resolves an instance of the specified type using a qualifier to
     * differentiate between multiple beans.
     *
     * @param <T> The type of the instance to resolve.
     * @param type The class type to resolve.
     * @param qualifier The qualifier used to identify the correct bean.
     * @return An instance of the resolved type.
     * @throws RuntimeException If no bean is found or multiple beans exist with
     * the same qualifier.
     */
    public <T> T resolveQualified(Class<T> type, String qualifier) {
        List<BeanDefinition> candidates = beanDefinitions.values().stream()
                .filter(bd -> type.isAssignableFrom(bd.getType()))
                .filter(bd -> Objects.equals(qualifier, bd.getQualifier()))
                .collect(Collectors.toList());

        if (candidates.isEmpty()) {
            throw new RuntimeException("No bean found for type: " + type.getName() + " with qualifier: " + qualifier);
        }
        if (candidates.size() > 1) {
            throw new RuntimeException("Multiple beans found for type: " + type.getName() + " with qualifier: " + qualifier);
        }
        BeanDefinition definition = candidates.get(0);
        BeanKey key = new BeanKey(definition.getType(), definition.getQualifier());
        return resolveDefinition(key, definition);
    }

    /**
     * Creates and stores a singleton instance of the specified bean definition.
     *
     * @param key The key representing the bean.
     * @param definition The bean definition.
     */
    private void createAndStoreSingleton(BeanKey key, BeanDefinition definition) {
        singletons.computeIfAbsent(key, k -> createInstance(definition.getType()));
    }

    /**
     * Resolves a bean definition, returning either a singleton instance or a
     * new instance.
     *
     * @param <T> The type of the bean.
     * @param key The key representing the bean.
     * @param definition The bean definition.
     * @return The resolved bean instance.
     */
    private <T> T resolveDefinition(BeanKey key, BeanDefinition definition) {
        if (definition.isSingleton()) {
            synchronized (singletons) {
                if (!singletons.containsKey(key)) {
                    Object instance = createInstance(definition.getType());
                    singletons.put(key, instance);
                }
                return (T) singletons.get(key);
            }
        } else {
            return (T) createInstance(definition.getType());
        }
    }

    /**
     * Initializes a bean by applying post-processors and invoking lifecycle
     * methods.
     *
     * @param bean The bean instance to initialize.
     * @param beanName The name of the bean.
     */
    private void initializeBean(Object bean, String beanName) {
        if (initializedBeans.contains(bean)) {
            return;
        }

        Object processedBean = applyBeanPostProcessorsBeforeInitialization(bean, beanName);
        invokePostConstruct(processedBean);
        processedBean = applyBeanPostProcessorsAfterInitialization(processedBean, beanName);
        initializedBeans.add(processedBean);
    }

    /**
     * Applies all registered bean post-processors before initialization.
     *
     * @param bean The bean instance.
     * @param beanName The name of the bean.
     * @return The processed bean instance.
     */
    private Object applyBeanPostProcessorsBeforeInitialization(Object bean, String beanName) {
        Object result = bean;
        for (BeanPostProcessor processor : postProcessors) {
            result = processor.postProcessBeforeInitialization(result, beanName);
        }
        return result;
    }

    /**
     * Applies all registered bean post-processors after initialization.
     *
     * @param bean The bean instance.
     * @param beanName The name of the bean.
     * @return The processed bean instance.
     */
    private Object applyBeanPostProcessorsAfterInitialization(Object bean, String beanName) {
        Object result = bean;
        for (BeanPostProcessor processor : postProcessors) {
            result = processor.postProcessAfterInitialization(result, beanName);
        }
        return result;
    }

    /**
     * Invokes methods annotated with @PostConstruct on the given bean.
     *
     * @param bean The bean instance.
     * @throws RuntimeException If an error occurs while invoking the method.
     */
    private void invokePostConstruct(Object bean) {
        for (Method method : bean.getClass().getDeclaredMethods()) {
            if (method.isAnnotationPresent(PostConstruct.class)) {
                try {
                    method.setAccessible(true);
                    method.invoke(bean);
                } catch (IllegalAccessException | InvocationTargetException e) {
                    throw new RuntimeException("Error invoking @PostConstruct method", e);
                }
            }
        }
    }

    /**
     * Destroys all singleton beans by invoking their @PreDestroy methods and
     * clearing caches.
     */
    public void destroy() {
        singletons.values().forEach(this::invokePreDestroy);
        singletons.clear();
        initializedBeans.clear();
    }

    /**
     * Invokes methods annotated with @PreDestroy on the given bean.
     *
     * @param bean The bean instance.
     * @throws RuntimeException If an error occurs while invoking the method.
     */
    private void invokePreDestroy(Object bean) {
        for (Method method : bean.getClass().getDeclaredMethods()) {
            if (method.isAnnotationPresent(PreDestroy.class)) {
                try {
                    method.setAccessible(true);
                    method.invoke(bean);
                } catch (IllegalAccessException | InvocationTargetException e) {
                    throw new RuntimeException("Error invoking @PreDestroy method", e);
                }
            }
        }
    }

    /**
     * Determines the qualifier for a given class based on its @Qualifier
     * annotation.
     *
     * @param type The class type.
     * @return The qualifier value or the lowercase simple name of the class if
     * no qualifier is present.
     */
    private String determineQualifier(Class<?> type) {
        Qualifier qualifier = type.getAnnotation(Qualifier.class);
        return (qualifier != null) ? qualifier.value() : type.getSimpleName().toLowerCase();
    }

    /**
     * Determines whether a given class should be treated as a singleton based
     * on its @Scope annotation.
     *
     * @param type The class type.
     * @return True if the class is a singleton, false otherwise.
     */
    private boolean isSingleton(Class<?> type) {
        Scope scope = type.getAnnotation(Scope.class);
        return (scope == null || ScopeType.SINGLETON == scope.value());
    }

    /**
     * Returns the map of singleton beans managed by the injector.
     *
     * @return A map of singleton beans.
     */
    public Map<BeanKey, Object> getSingletons() {
        return singletons;
    }

    /**
     * Validates the dependencies of a bean by checking its constructor and
     * field dependencies.
     *
     * @param beanType The class type of the bean.
     * @throws BeanValidationException If any required dependencies are missing.
     */
    private void validateBeanDependencies(Class<?> beanType) throws BeanValidationException {
        validateConstructorDependencies(beanType);
        validateFieldDependencies(beanType);
    }

    /**
     * Validates the dependencies required by a bean's constructor.
     *
     * @param beanType The class type of the bean.
     * @throws BeanValidationException If any required dependencies are missing.
     */
    private void validateConstructorDependencies(Class<?> beanType) throws BeanValidationException {
        Constructor<?> constructor = findInjectableConstructor(beanType);
        if (constructor == null) {
            return;
        }

        for (Parameter param : constructor.getParameters()) {
            String qualifier = getQualifierFromParameter(param);
            validateDependency(param.getType(), qualifier);
        }
    }

    /**
     * Validates the dependencies required by a bean's fields.
     *
     * @param beanType The class type of the bean.
     * @throws BeanValidationException If any required dependencies are missing.
     */
    private void validateFieldDependencies(Class<?> beanType) throws BeanValidationException {
        for (Field field : beanType.getDeclaredFields()) {
            if (field.isAnnotationPresent(Inject.class)) {
                String qualifier = getQualifierFromField(field);
                validateDependency(field.getType(), qualifier);
            }
        }
    }

    /**
     * Validates that a required dependency exists in the bean definitions.
     *
     * @param type The class type of the dependency.
     * @param qualifier The qualifier of the dependency.
     * @throws BeanValidationException If the dependency is missing.
     */
    private void validateDependency(Class<?> type, String qualifier) throws BeanValidationException {
        BeanKey key = new BeanKey(type, qualifier);
        if (!beanDefinitions.containsKey(key)) {
            throw new BeanValidationException("Missing dependency: " + key);
        }
    }

    /**
     * Validates all registered beans to ensure their dependencies are correctly
     * defined.
     *
     * @throws BeanValidationException If any bean validation fails.
     */
    private void validateBeans() throws BeanValidationException {
        for (Map.Entry<BeanKey, BeanDefinition> entry : beanDefinitions.entrySet()) {
            try {
                validateBeanDependencies(entry.getValue().getType());
            } catch (BeanValidationException e) {
                throw new BeanValidationException(
                        "Validation failed for bean " + entry.getKey() + ": " + e.getMessage()
                );
            }
        }
    }

    /**
     * Creates a Lazy<T> instance for a field with a generic type.
     *
     * @param field The field requiring lazy initialization.
     * @return A Lazy<T> instance for the specified field type.
     * @throws BeanCreationException If the field does not have a valid generic
     * type.
     */
    private Object createLazyForField(Field field) throws BeanCreationException {
        Type genericType = field.getGenericType();
        if (genericType instanceof ParameterizedType parameterizedType) {
            Type[] typeArgs = parameterizedType.getActualTypeArguments();
            Class<?> targetType = (Class<?>) typeArgs[0];
            return new Lazy<>(this, targetType);
        }
        throw new BeanCreationException("Invalid Lazy type for field: " + field.getName());
    }

    /**
     * Retrieves the qualifier value from a constructor parameter.
     *
     * @param param The parameter to inspect.
     * @return The qualifier value if present, otherwise the lowercase simple
     * name of the parameter type.
     */
    private String getQualifierFromParameter(Parameter param) {
        Qualifier qualifier = param.getAnnotation(Qualifier.class);
        return qualifier != null ? qualifier.value() : param.getType().getSimpleName().toLowerCase();
    }

    /**
     * Retrieves the qualifier value from a field.
     *
     * @param field The field to inspect.
     * @return The qualifier value if present, otherwise the lowercase simple
     * name of the field type.
     */
    private String getQualifierFromField(Field field) {
        Qualifier qualifier = field.getAnnotation(Qualifier.class);
        return qualifier != null ? qualifier.value() : field.getType().getSimpleName().toLowerCase();
    }

    /**
     * Initializes all singleton beans that are not marked as lazy. This ensures
     * eager initialization of required components.
     */
    private void initializeNonLazySingletons() {
        beanDefinitions.entrySet().stream()
                .filter(entry -> entry.getValue().isSingleton())
                .filter(entry -> !entry.getValue().isLazy())
                .forEach(entry -> {
                    BeanKey key = entry.getKey();
                    if (!singletons.containsKey(key)) {
                        singletons.put(key, createInstance(entry.getValue().getType()));
                    }
                });
    }
}
