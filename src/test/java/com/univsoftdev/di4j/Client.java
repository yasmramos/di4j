package com.univsoftdev.di4j;

import com.univsoftdev.di4j.annotations.*;

@Lazy
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
