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

    @Override
    protected void configure() {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }

    @Override
    public void configure(Object binder) {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }
}
