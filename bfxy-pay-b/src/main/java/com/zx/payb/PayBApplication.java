package com.zx.payb;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.zx.payb.mapper")
public class PayBApplication {
    public static void main(String[] args) {
        SpringApplication.run(PayBApplication.class, args);
    }
}
