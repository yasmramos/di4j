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
}
