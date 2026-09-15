package com.kite.libai;

import static org.assertj.core.api.Assertions.assertThat;

import com.kite.libai.hello.HelloController;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class KiteLibaiApplicationTests {

    @Autowired
    private HelloController helloController;

    @Test
    void contextLoads() {
        assertThat(helloController).isNotNull();
    }
}
