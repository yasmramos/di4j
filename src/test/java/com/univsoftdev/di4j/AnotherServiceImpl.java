package com.univsoftdev.di4j;

import com.univsoftdev.di4j.examples.ServiceInterface;

/**
 *
 * @author CONTADOR
 */
public class AnotherServiceImpl implements AnotherService {

    public AnotherServiceImpl(ServiceInterface service) {
    }

    @Override
    public String doSomething() {
        return "ServiceImpl is serving";
    }

}
