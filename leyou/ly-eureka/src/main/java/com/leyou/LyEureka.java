package com.leyou;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.server.EnableEurekaServer;

/**
 * author:lu
 * create time: 2019/11/19.
 */
@EnableEurekaServer
@SpringBootApplication
public class LyEureka {
    public static void main(String[] args) {
        SpringApplication.run(LyEureka.class);
    }
}
