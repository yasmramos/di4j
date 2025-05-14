package com.univsoftdev.di4j;

import java.io.File;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.net.JarURLConnection;
import java.net.URL;
import java.net.URLDecoder;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public class ClassScanner {

    private static final Set<Class<? extends Annotation>> COMPONENT_ANNOTATIONS = Set.of(
        com.univsoftdev.di4j.annotations.Component.class,
        com.univsoftdev.di4j.annotations.Service.class,
        com.univsoftdev.di4j.annotations.Repository.class,
        com.univsoftdev.di4j.annotations.Controller.class
    );

    public static Set<Class<?>> scanForComponents(String basePackage) {
        Set<Class<?>> classes = new HashSet<>();
        String path = basePackage.replace('.', '/');

        try {
            ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
            Enumeration<URL> resources = classLoader.getResources(path);

            while (resources.hasMoreElements()) {
                URL resource = resources.nextElement();
                String protocol = resource.getProtocol();

                if ("file".equals(protocol)) {
                    String filePath = URLDecoder.decode(resource.getFile(), "UTF-8");
                    File directory = new File(filePath);
                    classes.addAll(findClasses(directory, basePackage));
                } else if ("jar".equals(protocol)) {
                    JarURLConnection jarConn = (JarURLConnection) resource.openConnection();
                    JarFile jarFile = jarConn.getJarFile();
                    Enumeration<JarEntry> entries = jarFile.entries();

                    while (entries.hasMoreElements()) {
                        JarEntry entry = entries.nextElement();
                        String entryName = entry.getName();

                        if (entryName.startsWith(path) && entryName.endsWith(".class") && !entry.isDirectory()) {
                            String className = entryName.replace('/', '.').substring(0, entryName.length() - 6);
                            try {
                                Class<?> clazz = Class.forName(className);
                                if (isComponent(clazz)) {
                                    classes.add(clazz);
                                }
                            } catch (ClassNotFoundException e) {
                                throw new RuntimeException("Failed to load class from JAR: " + className, e);
                            }
                        }
                    }
                }
            }
        } catch (IOException e) {
            throw new RuntimeException("Error scanning package: " + basePackage, e);
        }

        return classes;
    }

    private static Set<Class<?>> findClasses(File directory, String packageName) {
        Set<Class<?>> classes = new HashSet<>();
        if (!directory.exists()) {
            return classes;
        }

        File[] files = directory.listFiles();
        if (files == null) {
            return classes;
        }

        for (File file : files) {
            if (file.isDirectory()) {
                classes.addAll(findClasses(file, packageName + "." + file.getName()));
            } else if (file.getName().endsWith(".class")) {
                String className = packageName + '.' + file.getName().substring(0, file.getName().length() - 6);
                try {
                    Class<?> clazz = Class.forName(className);
                    if (isComponent(clazz)) {
                        classes.add(clazz);
                    }
                } catch (ClassNotFoundException e) {
                    throw new RuntimeException("Failed to load class: " + className, e);
                }
            }
        }
        return classes;
    }

    private static boolean isComponent(Class<?> clazz) {
        for (Class<? extends Annotation> annotation : COMPONENT_ANNOTATIONS) {
            if (clazz.isAnnotationPresent(annotation)) {
                return true;
            }
        }
        return false;
    }
}
