package com.aidocs.aiservice.test.annotations;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import java.lang.annotation.*;
import java.util.concurrent.TimeUnit;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Test
@Tag("contract")
@Tag("api")
@Timeout(value = 30, unit = TimeUnit.SECONDS)
public @interface ContractTest {
}
