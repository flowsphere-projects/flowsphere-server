package com.flowsphere.server;


import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@EnableJpaRepositories
@SpringBootApplication
public class FlowSphereServerApplication {

    public static void main(String[] args) {
        SpringApplication.run(FlowSphereServerApplication.class, args);
    }

}
