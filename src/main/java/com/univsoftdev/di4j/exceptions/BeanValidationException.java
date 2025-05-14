/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.univsoftdev.di4j.exceptions;

/**
 *
 * @author CONTADOR
 */
public class BeanValidationException extends Exception{

    public BeanValidationException() {
    }

    public BeanValidationException(String message) {
        super(message);
    }

    public BeanValidationException(String message, Throwable cause) {
        super(message, cause);
    }

    public BeanValidationException(Throwable cause) {
        super(cause);
    }

    public BeanValidationException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
    
}
