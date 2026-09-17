package com.kite.libai;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
// 扫描并注册 @ConfigurationProperties 类(SecurityProperties 使用构造器绑定,需要显式注册)
@ConfigurationPropertiesScan
public class KiteLibaiApplication {

    public static void main(String[] args) {
        SpringApplication.run(KiteLibaiApplication.class, args);
    }
}
