package com.kite.libai.hello;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

/**
 * Plain unit test: no Spring context, fixed clock for deterministic assertions.
 */
class HelloServiceTest {

    private static final Instant FIXED_NOW = Instant.parse("2026-09-15T10:15:30Z");

    private final HelloService helloService =
            new HelloService(Clock.fixed(FIXED_NOW, ZoneOffset.UTC));

    @Test
    void greetsTheGivenName() {
        HelloResponse response = helloService.greet("Kite");

        assertThat(response.getMessage()).isEqualTo("Hello, Kite!");
        assertThat(response.getTimestamp()).isEqualTo(FIXED_NOW);
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" ", "   ", "\t"})
    void fallsBackToWorldWhenNameIsMissing(String name) {
        assertThat(helloService.greet(name).getMessage()).isEqualTo("Hello, World!");
    }

    @Test
    void trimsSurroundingWhitespace() {
        assertThat(helloService.greet("  Kite  ").getMessage()).isEqualTo("Hello, Kite!");
    }
}
