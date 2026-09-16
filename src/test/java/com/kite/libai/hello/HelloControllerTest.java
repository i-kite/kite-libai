package com.kite.libai.hello;

import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.kite.libai.common.exception.GlobalExceptionHandler;
import java.time.Instant;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

/**
 * Web layer slice test: only the MVC infrastructure, {@link HelloController} and the
 * exception handler are loaded, the service is replaced by a Mockito mock.
 */
@WebMvcTest(HelloController.class)
@Import(GlobalExceptionHandler.class)
class HelloControllerTest {

    private static final Instant FIXED_NOW = Instant.parse("2026-09-15T10:15:30Z");

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private HelloService helloService;

    @Test
    void returnsDefaultGreetingWithoutQueryParam() throws Exception {
        given(helloService.greet(isNull())).willReturn(new HelloResponse("Hello, World!", FIXED_NOW));

        mockMvc.perform(get("/api/hello"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.message").value("Hello, World!"))
                .andExpect(jsonPath("$.timestamp").value("2026-09-15T10:15:30Z"));

        verify(helloService).greet(null);
    }

    @Test
    void returnsGreetingForQueryParam() throws Exception {
        given(helloService.greet("Kite")).willReturn(new HelloResponse("Hello, Kite!", FIXED_NOW));

        mockMvc.perform(get("/api/hello").param("name", "Kite"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Hello, Kite!"));
    }

    @Test
    void returnsGreetingForPathVariable() throws Exception {
        given(helloService.greet("Kite")).willReturn(new HelloResponse("Hello, Kite!", FIXED_NOW));

        mockMvc.perform(get("/api/hello/{name}", "Kite"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Hello, Kite!"));
    }

    @Test
    void rejectsNameLongerThanFiftyCharacters() throws Exception {
        StringBuilder tooLong = new StringBuilder();
        for (int i = 0; i < 51; i++) {
            tooLong.append('x');
        }

        mockMvc.perform(get("/api/hello").param("name", tooLong.toString()))
                .andExpect(status().isBadRequest())
                // 全局异常处理器已统一为 R 结构,校验失败返回 code = 400
                .andExpect(jsonPath("$.code").value(400));
    }

    @Test
    void returnsNotFoundForUnknownPath() throws Exception {
        mockMvc.perform(get("/api/unknown")).andExpect(status().isNotFound());
    }
}
