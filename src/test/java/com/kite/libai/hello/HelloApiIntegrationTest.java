package com.kite.libai.hello;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

/**
 * Full-stack test: the application boots on a random port and is called over real HTTP.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class HelloApiIntegrationTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    void helloEndpointReturnsDefaultGreeting() {
        ResponseEntity<HelloResponse> response =
                restTemplate.getForEntity("/api/hello", HelloResponse.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getMessage()).isEqualTo("Hello, World!");
        assertThat(response.getBody().getTimestamp()).isNotNull();
    }

    @Test
    void helloEndpointReturnsGreetingForName() {
        ResponseEntity<HelloResponse> response =
                restTemplate.getForEntity("/api/hello?name={name}", HelloResponse.class, "Kite");

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getMessage()).isEqualTo("Hello, Kite!");
    }

    @Test
    void rejectsTooLongName() {
        StringBuilder tooLong = new StringBuilder();
        for (int i = 0; i < 51; i++) {
            tooLong.append('x');
        }

        ResponseEntity<String> response =
                restTemplate.getForEntity("/api/hello?name={name}", String.class, tooLong.toString());

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }
}
