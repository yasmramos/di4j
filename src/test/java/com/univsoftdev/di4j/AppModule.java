package com.univsoftdev.di4j;

// Removed @Provides and old configure methods to align with AbstractModule changes.
// This AppModule is likely a test utility that needs to conform to the new module structure.
public class AppModule extends AbstractModule {

    @Override
    protected void configureBindings() {
        // Add bindings here if this module is used in other tests.
        // For now, providing an empty implementation to make it compile.
        // Example: bind(MyExampleInterface.class, MyExampleImplementation.class);
    }

    // The problematic configure(Object binder) and configure() methods are removed.
    // The @Provides method provideAnotherService is also removed as it's not
    // directly related to AbstractModule's binding mechanism in this context.
    // If @Provides is a separate feature, it needs its own handling/processor.
}
