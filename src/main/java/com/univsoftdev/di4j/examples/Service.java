package com.univsoftdev.di4j.examples;

import com.univsoftdev.di4j.annotations.Component;

@Component
public class Service {
    public void serve() {
        System.out.println("Service is serving.");
    }
}

