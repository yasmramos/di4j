package com.univsoftdev.di4j;

import java.io.IOException;
import java.util.Properties;

public interface ConfigurationSource {

    Properties load() throws IOException;
}
