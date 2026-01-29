package com.mobileAutomation.driver;

public enum Platform {
    ANDROID,
    IOS;

    public static Platform fromString(String value) {
        if (value == null) {
            throw new IllegalArgumentException(
                    "Platform is not specified. Use -Dplatform=android or -Dplatform=ios"
            );
        }
        return Platform.valueOf(value.toUpperCase());
    }
}
