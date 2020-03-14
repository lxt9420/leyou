package com.leyou.test;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.HashMap;
import java.util.Map;

/**
 * author:lu
 * create time: 2020/2/26.
 */
@RunWith(SpringRunner.class)
@SpringBootTest
public class SmsTest {
    @Autowired
    private AmqpTemplate amqpTemplate;
    @Test
    public void testSend() throws InterruptedException {
        Map<String,String> smg=new HashMap<>();
        smg.put("phone","15347840591");
        smg.put("code","54321");
        amqpTemplate.convertAndSend("ly.sms.exchange","sms.verify.code",smg);
        Thread.sleep(10000L);
    }
}
