package com.univsoftdev.di4j.exceptions;

public class BeanResolutionException extends RuntimeException {

    public BeanResolutionException(String message) {
        super(message);
    }

    public BeanResolutionException() {
    }

    public BeanResolutionException(String message, Throwable cause) {
        super(message, cause);
    }

    public BeanResolutionException(Throwable cause) {
        super(cause);
    }

    public BeanResolutionException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

}
