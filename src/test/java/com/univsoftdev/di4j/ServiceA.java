package com.univsoftdev.di4j;

import com.univsoftdev.di4j.annotations.Component;
import com.univsoftdev.di4j.annotations.Inject;
import java.util.function.Supplier;

@Component
class ServiceA {

    private final Supplier<ServiceB> serviceB;

    @Inject
    public ServiceA(Supplier<ServiceB> serviceB) {
        this.serviceB = serviceB;
    }
}
