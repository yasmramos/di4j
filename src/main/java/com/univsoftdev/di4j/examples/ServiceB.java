package com.univsoftdev.di4j.examples;

import com.univsoftdev.di4j.annotations.Component;
import com.univsoftdev.di4j.annotations.Inject;

@Component
class ServiceB {
    @Inject
    public ServiceB(ServiceA serviceA) { /* ... */ }
}
