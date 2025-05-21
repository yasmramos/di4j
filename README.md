# **DI4J - Inyección de Dependencias Ligera para Java**  

DI4J es un **framework ligero de inyección de dependencias** para Java, inspirado en **Google Guice**, pero diseñado para ser **simple y flexible**. Permite a los desarrolladores gestionar dependencias de manera eficiente utilizando **módulos, proveedores e interceptores**.  

## **Características**  
✅ **Configuración basada en módulos** (similar a `AbstractModule` de Guice).  
✅ **Resolución automática de dependencias** con `Injector`.  
✅ **Soporte para inicialización de singleton y lazy**.  
✅ **Proveedores para creación dinámica de instancias**.  
✅ **Interceptores para modificar el comportamiento de métodos**.  
✅ **Inyección de dependencias basada en anotaciones (`@Inject`, `@Singleton`, `@Provides`)**.  

---

## **Instalación**  

Para utilizar DI4J en tu proyecto Maven, agrega la siguiente dependencia a tu archivo `pom.xml`:

```xml
<dependency>
    <groupId>com.univsoftdev.di4j</groupId>
    <artifactId>di4j</artifactId>
    <version>1.0</version>
</dependency>
```

Asegúrate de reemplazar `1.0` con la versión más reciente si es necesario.
