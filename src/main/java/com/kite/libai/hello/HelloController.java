package com.kite.libai.hello;

import javax.validation.constraints.Size;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(path = "/api", produces = MediaType.APPLICATION_JSON_VALUE)
@Validated
public class HelloController {

    private final HelloService helloService;

    public HelloController(HelloService helloService) {
        this.helloService = helloService;
    }

    @GetMapping("/hello")
    public HelloResponse hello(@RequestParam(required = false) @Size(max = 50) String name) {
        return helloService.greet(name);
    }

    @GetMapping("/hello/{name}")
    public HelloResponse helloByPath(@PathVariable @Size(min = 1, max = 50) String name) {
        return helloService.greet(name);
    }
}
