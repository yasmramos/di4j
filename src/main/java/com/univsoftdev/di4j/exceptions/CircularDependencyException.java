package com.univsoftdev.di4j.exceptions;

public class CircularDependencyException extends RuntimeException {

    public CircularDependencyException(String message) {
        super(message);
    }
}
