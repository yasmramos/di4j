package com.univsoftdev.di4j;

import com.univsoftdev.di4j.ScopeType;
import com.univsoftdev.di4j.annotations.*;

@Component
@Scope(ScopeType.SINGLETON)
@Qualifier("primary")
public class ServiceImpl implements ServiceInterface {

    @PostConstruct
    public void init() {
        System.out.println("Service initialized");
    }

    @PreDestroy
    public void cleanup() {
        System.out.println("Service cleaning up");
    }

    @Override
    public void serve() {
        System.out.println("Service implementation is serving.");
    }
}
