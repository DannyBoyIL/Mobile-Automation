package com.mobileAutomation.config;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.InputStream;

public class DeviceConfig {

    public static <T> T load(String fileName, Class<T> clazz) {
        try (InputStream is = DeviceConfig.class
                .getClassLoader()
                .getResourceAsStream("config/" + fileName)) {

            if (is == null) {
                throw new RuntimeException("Config file not found: " + fileName);
            }

            return new ObjectMapper()
                    .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
                    .readValue(is, clazz);
        } catch (Exception e) {
            throw new RuntimeException("Failed to load config: " + fileName, e);
        }
    }
}
