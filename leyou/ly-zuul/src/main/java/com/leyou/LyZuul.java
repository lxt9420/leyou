package com.leyou;

import org.springframework.boot.SpringApplication;
import org.springframework.cloud.client.SpringCloudApplication;
import org.springframework.cloud.netflix.zuul.EnableZuulProxy;

/**
 * author:lu
 * create time: 2019/11/21.
 */
@EnableZuulProxy
@SpringCloudApplication
public class LyZuul {
    public static void main(String[] args) {
        SpringApplication.run(LyZuul.class);
    }
}
