package com.kite.libai.hello;

import java.time.Clock;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class HelloService {

    private static final String DEFAULT_NAME = "World";

    private final Clock clock;

    public HelloService(Clock clock) {
        this.clock = clock;
    }

    /**
     * Builds a greeting for the given name, falling back to {@code World}
     * when the name is null or blank.
     */
    public HelloResponse greet(String name) {
        String target = StringUtils.hasText(name) ? name.trim() : DEFAULT_NAME;
        return new HelloResponse(String.format("Hello, %s!", target), clock.instant());
    }
}
