package com.univsoftdev.di4j;

import com.univsoftdev.di4j.annotations.Component;
import com.univsoftdev.di4j.annotations.Inject;
import com.univsoftdev.di4j.examples.ServiceInterface;

@Component
public class Cliente {
    private final ServiceInterface service;

    @Inject
    public Cliente(ServiceInterface service) {
        this.service = service;
    }

    public void doSomething() {
        service.serve();
    }
}
