package com.aidocs.aiservice.test.annotations;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import java.lang.annotation.*;
import java.util.concurrent.TimeUnit;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Test
@Tag("unit")
@Tag("controller")
@Timeout(value = 10, unit = TimeUnit.SECONDS)
public @interface ControllerTest {
}
