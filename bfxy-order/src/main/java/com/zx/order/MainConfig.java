package com.zx.order;


import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

/**
 * @author Mr.xin
 */
@Configuration
@ComponentScan(basePackages = {"com.zx.order.*"})
@MapperScan(basePackages = {"com.zx.order.m"})
public class MainConfig {

}