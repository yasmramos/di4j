package com.univsoftdev.di4j;

import java.util.Objects;

public class BeanKey {

    private final Class<?> type;
    private final String qualifier;

    public BeanKey(Class<?> type, String qualifier) {
        this.type = type;
        this.qualifier = qualifier;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        BeanKey beanKey = (BeanKey) o;
        return Objects.equals(type, beanKey.type)
                && Objects.equals(qualifier, beanKey.qualifier);
    }

    @Override
    public int hashCode() {
        return Objects.hash(type, qualifier);
    }

    @Override
    public String toString() {
        String defaultQualifier = type.getSimpleName().toLowerCase();
        return qualifier.equals(defaultQualifier)
                ? type.getSimpleName()
                : type.getSimpleName() + "@" + qualifier;
    }
}
