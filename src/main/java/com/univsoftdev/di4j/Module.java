package com.univsoftdev.di4j;

@FunctionalInterface
public interface Module {

    void configure(Binder binder);
}
