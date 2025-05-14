package com.univsoftdev.di4j;

public class AnotherServiceImpl implements AnotherService {

    public AnotherServiceImpl(ServiceInterface service) {
    }

    @Override
    public String doSomething() {
        return "ServiceImpl is serving";
    }

}
