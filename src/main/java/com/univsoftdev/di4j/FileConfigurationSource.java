package com.univsoftdev.di4j;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class FileConfigurationSource implements ConfigurationSource {

    private final String filePath;

    public FileConfigurationSource(String filePath) {
        this.filePath = filePath;
    }

    @Override
    public Properties load() throws IOException {
        Properties properties = new Properties();
        try (InputStream input = getClass().getClassLoader().getResourceAsStream(filePath)) {
            if (input == null) {
                throw new FileNotFoundException("Property file '" + filePath + "' not found");
            }
            properties.load(input);
        }
        return properties;
    }
}
