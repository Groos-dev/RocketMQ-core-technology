package com.zx.store;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@MapperScan("com.zx.store.mapper")
@ComponentScan({"com.zx.store"})
public class MainConfig {
}
