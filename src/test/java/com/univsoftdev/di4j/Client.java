package com.univsoftdev.di4j;

import com.univsoftdev.di4j.annotations.Component;
import com.univsoftdev.di4j.annotations.Inject;
import com.univsoftdev.di4j.annotations.Qualifier;
// Using fully qualified name for @Lazy to avoid conflict with com.univsoftdev.di4j.Lazy class
@com.univsoftdev.di4j.annotations.Lazy 
@Component
public class Client {

    private final ServiceInterface service;

    @Inject
    public Client(@Qualifier("primary") ServiceInterface service) {
        this.service = service;
    }

    public void doSomething() {
        service.serve();
    }
}
