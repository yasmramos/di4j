/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.univsoftdev.di4j;

import com.univsoftdev.di4j.annotations.Component;
import com.univsoftdev.di4j.annotations.Lazy;
import com.univsoftdev.di4j.annotations.PostConstruct;

@Lazy
@Component
public class LazyService {

    public LazyService() {
        System.out.println("Creando LazyService");
    }

    private boolean initialized = false;

    @PostConstruct
    public void init() {
        initialized = true;
    }

    public boolean isInitialized() {
        return initialized;
    }
}
