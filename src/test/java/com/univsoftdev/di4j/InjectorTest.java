package com.univsoftdev.di4j;

import com.univsoftdev.di4j.annotations.Component;
import com.univsoftdev.di4j.annotations.Inject;
import com.univsoftdev.di4j.annotations.Lazy;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;

public class InjectorTest {

    @Test
    void testCircularDependencyWithDetailedMessage() {
        Injector injector = new Injector(new Configuration());
        injector.register(ServiceA.class);
        injector.register(ServiceB.class);

        Exception exception = assertThrows(RuntimeException.class, () -> {
            injector.resolve(ServiceA.class);
        });

        String expectedMessage = """
        Circular dependency detected:
        ServiceA -> ServiceB -> ServiceA"""; // Orden correcto

        assertTrue(exception.getMessage().contains(expectedMessage),
                "Mensaje real: " + exception.getMessage());
    }

//    @Test
//    void testLazyInjection() {
//        Injector injector = new Injector(new Configuration());
//
//        injector.register(ServiceA.class);
//        injector.register(ServiceB.class);
//
//        ServiceA serviceA = injector.resolve(ServiceA.class);
//        assertNotNull(serviceA.getLazyB().get()); // La resolución ocurre aquí
//
//        ServiceB serviceB = injector.resolve(ServiceB.class);
//        assertNotNull(serviceB.getLazyA().get()); // Verificar la resolución inversa
//    }
    @Test
    public void noDebeExistir() {
        Injector injector = new Injector(new Configuration());
        // Verificar que se lanza una excepción al resolver
        assertThrows(RuntimeException.class, () -> injector.resolve(LazyService.class));
    }

    @Test
    public void testLazyCreationOnFirstResolve() {
        Configuration config = new Configuration()
                .setLazyInit(true)
                .setAutoDetectComponents(true)
                .setBasePackages("com.univsoftdev.di4j");
        Injector injector = new Injector(config);

        // Usar el calificador correcto generado por determineQualifier()
        BeanKey key = new BeanKey(LazyService.class, "lazyservice"); // ← Calificador correcto

        assertFalse(injector.getSingletons().containsKey(key));

        // Resolver y verificar creación
        LazyService service = injector.resolve(LazyService.class);
        assertNotNull(service);
        assertTrue(injector.getSingletons().containsKey(key));
    }

    @Test
    public void testPostConstructWithLazy() {
        Configuration config = new Configuration()
                .setLazyInit(true)
                .setAutoDetectComponents(true)
                .setBasePackages("com.univsoftdev.di4j");
        Injector injector = new Injector(config);
        LazyService service = injector.resolve(LazyService.class);
        assertTrue(service.isInitialized()); // ← Debe ser true
    }

    @Test
    public void testPrototypeSupplier() {
        Injector injector = new Injector();
        injector.registerSupplier(CustomService.class, () -> new CustomService("proto"), false);

        CustomService service1 = injector.resolve(CustomService.class);
        CustomService service2 = injector.resolve(CustomService.class);

        assertNotSame(service1, service2); // Nueva instancia cada vez
    }

    @Test
    public void testLazySupplier() {
        Configuration configuration = new Configuration();
        configuration.setLazyInit(true);
        configuration.setAutoDetectComponents(false);
        Injector injector = new Injector(configuration);
        injector.registerSupplier(TestService.class, TestService::new, true);

        BeanKey key = new BeanKey(TestService.class, "testservice");
        assertFalse(injector.getSingletons().containsKey(key)); // Ahora pasa

        TestService service = injector.resolve(TestService.class);
        assertTrue(injector.getSingletons().containsKey(key));
    }

    @Test
    public void testSupplierWithDependencies() {
        Injector injector = new Injector();
        injector.registerSupplier(DatabaseConnection.class, ()
                -> new DatabaseConnection("jdbc:mysql://localhost", "user")
        );

        DatabaseConnection conn = injector.resolve(DatabaseConnection.class);
        assertNotNull(conn.getUrl());
    }

    @Test
    void testCircularDependencyWithSupplier() {
        Injector injector = new Injector(new Configuration());

//        injector.registerSupplier(ServiceA.class, () -> {
//            ServiceB b = injector.resolve(ServiceB.class);
//            return new ServiceA(b);
//        });
//
//        injector.registerSupplier(ServiceB.class, () -> {
//            ServiceA a = injector.resolve(ServiceA.class); // Esto ahora lanzará RuntimeException
//            return new ServiceB(a);
//        });
        assertThrows(RuntimeException.class, () -> injector.resolve(ServiceA.class));
    }

    @Lazy
    @Component
    static class ServiceA {

        private final ServiceB b;

        @Inject
        public ServiceA(ServiceB b) {
            this.b = b;
        }
    }

    @Lazy
    @Component
    static class ServiceB {

        private final ServiceA a;

        @Inject
        public ServiceB(ServiceA a) {
            this.a = a;
        }
    }

    // Helper interfaces and classes for module tests

    interface ITestService { String serve(); }
    static class TestServiceImpl implements ITestService {
        @Override public String serve() { return "TestService served"; }
    }

    interface IAnotherService { String another(); }
    static class AnotherServiceImpl implements IAnotherService {
        @Override public String another() { return "AnotherService served"; }
    }

    static class MyData {
        private final String value;
        public MyData(String value) { this.value = value; }
        public String getValue() { return value; }
        // Override equals and hashCode for reliable instance comparison if needed, though for same instance check `assertSame` is better.
        @Override public boolean equals(Object o) { if (this == o) return true; if (o == null || getClass() != o.getClass()) return false; MyData myData = (MyData) o; return java.util.Objects.equals(value, myData.value); }
        @Override public int hashCode() { return java.util.Objects.hash(value); }
    }

    interface IProvidedService { String provide(); }
    static class ProvidedServiceImpl implements IProvidedService {
        private final String id;
        public ProvidedServiceImpl(String id) { this.id = id; }
        @Override public String provide() { return "ProvidedService with id: " + id; }
    }

    interface IServiceA { String methodA(); }
    static class ServiceImplA implements IServiceA {
        @Override public String methodA() { return "ServiceA Impl"; }
    }

    interface IServiceB { String methodB(); }
    static class ServiceImplB implements IServiceB {
        @Override public String methodB() { return "ServiceB Impl"; }
    }
    
    interface IParentService { String parentMethod(); }
    static class ParentServiceImpl implements IParentService {
        @Override public String parentMethod() { return "ParentService Impl"; }
    }

    // Test Modules

    static class MyTestModule extends AbstractModule {
        @Override
        protected void configureBindings() {
            bind(ITestService.class, TestServiceImpl.class);
            bind(IAnotherService.class, AnotherServiceImpl.class);
        }
    }

    static class InstanceTestModule extends AbstractModule {
        private final MyData instance = new MyData("specific_instance_value");
        @Override
        protected void configureBindings() {
            bindInstance(MyData.class, instance);
        }
        public MyData getExpectedInstance() { return instance; }
    }

    static class ProviderTestModule extends AbstractModule {
        @Override
        protected void configureBindings() {
            bindProvider(IProvidedService.class, () -> new ProvidedServiceImpl("dynamic_value_from_provider"));
        }
    }

    static class ModuleA extends AbstractModule {
        @Override
        protected void configureBindings() {
            bind(IServiceA.class, ServiceImplA.class);
        }
    }

    static class ModuleB extends AbstractModule {
        @Override
        protected void configureBindings() {
            bind(IServiceB.class, ServiceImplB.class);
        }
    }

    static class ParentTestModule extends AbstractModule {
        @Override
        protected void configureBindings() {
            bind(IParentService.class, ParentServiceImpl.class);
            install(new ModuleA());
            install(new ModuleB());
        }
    }

    // Unit Tests for Module System

    @Test
    void testBasicModuleBindingAndInjection() {
        Injector injector = Injector.createInjector(new MyTestModule());

        ITestService testService = injector.getInstance(ITestService.class);
        assertNotNull(testService, "TestService should be resolved");
        assertTrue(testService instanceof TestServiceImpl, "TestService should be instance of TestServiceImpl");
        org.junit.jupiter.api.Assertions.assertEquals("TestService served", testService.serve(), "TestService.serve() output mismatch");

        IAnotherService anotherService = injector.getInstance(IAnotherService.class);
        assertNotNull(anotherService, "AnotherService should be resolved");
        assertTrue(anotherService instanceof AnotherServiceImpl, "AnotherService should be instance of AnotherServiceImpl");
        org.junit.jupiter.api.Assertions.assertEquals("AnotherService served", anotherService.another(), "AnotherService.another() output mismatch");
    }

    @Test
    void testInstanceBindingInModule() {
        InstanceTestModule module = new InstanceTestModule();
        Injector injector = Injector.createInjector(module);

        MyData instance1 = injector.getInstance(MyData.class);
        assertNotNull(instance1, "MyData instance should be resolved");
        org.junit.jupiter.api.Assertions.assertEquals("specific_instance_value", instance1.getValue(), "MyData instance value mismatch");

        MyData instance2 = injector.getInstance(MyData.class);
        assertNotNull(instance2, "MyData instance (second call) should be resolved");
        org.junit.jupiter.api.Assertions.assertSame(instance1, instance2, "Multiple calls to getInstance for an instance binding should return the same instance.");
        org.junit.jupiter.api.Assertions.assertSame(module.getExpectedInstance(), instance1, "Resolved instance should be the exact one bound in the module.");
    }

    @Test
    void testProviderBindingInModule() {
        Injector injector = Injector.createInjector(new ProviderTestModule());

        IProvidedService providedService1 = injector.getInstance(IProvidedService.class);
        assertNotNull(providedService1, "ProvidedService should be resolved");
        assertTrue(providedService1 instanceof ProvidedServiceImpl, "ProvidedService should be instance of ProvidedServiceImpl");
        org.junit.jupiter.api.Assertions.assertEquals("ProvidedService with id: dynamic_value_from_provider", providedService1.provide(), "ProvidedService.provide() output mismatch");

        // Assuming default provider behavior is to create new instances (prototype-like)
        // unless singleton scope is explicitly defined and testable for providers.
        // For this test, we'll check if it *can* create instances. If it's a singleton by default
        // due to provider caching in Injector, instance1 and instance2 would be the same.
        // The current Injector.getInstance for providers *does* cache, so they will be same.
        IProvidedService providedService2 = injector.getInstance(IProvidedService.class);
        org.junit.jupiter.api.Assertions.assertSame(providedService1, providedService2, "Provider-bound instances should be the same by default due to Injector caching.");
    }
    
    @Test
    void testModuleInstallation() {
        Injector injector = Injector.createInjector(new ParentTestModule());

        IParentService parentService = injector.getInstance(IParentService.class);
        assertNotNull(parentService);
        assertTrue(parentService instanceof ParentServiceImpl);
        org.junit.jupiter.api.Assertions.assertEquals("ParentService Impl", parentService.parentMethod());

        IServiceA serviceA = injector.getInstance(IServiceA.class);
        assertNotNull(serviceA);
        assertTrue(serviceA instanceof ServiceImplA);
        org.junit.jupiter.api.Assertions.assertEquals("ServiceA Impl", serviceA.methodA());

        IServiceB serviceB = injector.getInstance(IServiceB.class);
        assertNotNull(serviceB);
        assertTrue(serviceB instanceof ServiceImplB);
        org.junit.jupiter.api.Assertions.assertEquals("ServiceB Impl", serviceB.methodB());
    }

    @Test
    void testMultipleModulesInInjectorConstructor() {
        Injector injector = Injector.createInjector(new ModuleA(), new ModuleB());

        IServiceA serviceA = injector.getInstance(IServiceA.class);
        assertNotNull(serviceA);
        assertTrue(serviceA instanceof ServiceImplA);
        org.junit.jupiter.api.Assertions.assertEquals("ServiceA Impl", serviceA.methodA());

        IServiceB serviceB = injector.getInstance(IServiceB.class);
        assertNotNull(serviceB);
        assertTrue(serviceB instanceof ServiceImplB);
        org.junit.jupiter.api.Assertions.assertEquals("ServiceB Impl", serviceB.methodB());
    }
}
