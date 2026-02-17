package com.mobileAutomation.assertions;

import org.testng.Assert;

public final class ResultAssertions {

    private ResultAssertions() {}

    public static <T> T assertInstanceOf(Class<T> expectedType, Object actual) {
        Assert.assertNotNull(actual, "Result was null");

        if (!expectedType.isInstance(actual)) {
            Assert.fail(
                    "Expected result type <" + expectedType.getSimpleName() + "> " +
                            "but was <" + actual.getClass().getSimpleName() + ">"
            );
        }

        return expectedType.cast(actual);
    }
}