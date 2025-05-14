package com.univsoftdev.di4j;

import com.univsoftdev.di4j.BeanPostProcessor;

public class LoggingPostProcessor implements BeanPostProcessor {

    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) {
        System.out.println("Initializing bean: " + beanName);
        return bean;
    }

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) {
        System.out.println("Finished initializing bean: " + beanName);
        return bean;
    }
}
