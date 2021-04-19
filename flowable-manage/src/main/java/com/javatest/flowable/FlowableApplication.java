package com.javatest.flowable;

import com.javatest.flowable.config.ApplicationConfiguration;
import com.javatest.flowable.servlet.AppDispatcherServletConfiguration;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Import;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@Import({
        ApplicationConfiguration.class,
        AppDispatcherServletConfiguration.class
})
@ComponentScan(basePackages = {"com.javatest.flowable"})
@MapperScan("com.javatest.flowable.dao")
@EnableTransactionManagement
@SpringBootApplication
public class FlowableApplication {
    public static void main(String[] args) {
        SpringApplication.run(FlowableApplication.class,args);
    }
}
