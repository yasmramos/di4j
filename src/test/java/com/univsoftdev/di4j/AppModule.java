package com.univsoftdev.di4j;

import com.univsoftdev.di4j.annotations.Provides;

/**
 *
 * @author CNA
 */
public class AppModule extends AbstractModule{

    /**
     *
     * @param service
     * @return
     */
    @Provides
    public AnotherService provideAnotherService(ServiceInterface service) {
        return new AnotherServiceImpl(service);
    }
}
