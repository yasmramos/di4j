package com.univsoftdev.di4j.examples;

import com.univsoftdev.di4j.AbstractModule;
import com.univsoftdev.di4j.Binder;
import java.util.logging.Logger;

public class BillingModule extends AbstractModule {

    @Override
    protected void configure() {
        // Bind a specific instance
        bindInstance(Logger.class, Logger.getLogger("BillingLogger"));
    }

    @Override
    public void configure(Binder binder) {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }
}
