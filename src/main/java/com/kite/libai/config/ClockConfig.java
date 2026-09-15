package com.kite.libai.config;

import java.time.Clock;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ClockConfig {

    /**
     * Exposing the clock as a bean keeps time-dependent logic deterministic in tests.
     */
    @Bean
    public Clock clock() {
        return Clock.systemUTC();
    }
}
