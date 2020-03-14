package com.leyou.order.controller;

import com.leyou.order.service.OrderService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * @author bystander
 * @date 2018/10/5
 */

@Slf4j
@RestController
@RequestMapping("notify")
public class NotifyController {

    @Autowired
    private OrderService orderService;

    @PostMapping(value = "pay",produces = "application/xml")
    public Map<String,String> payNotify(@RequestBody Map<String, String> result) {
        //处理回调结果
        orderService.handleNotify(result);
        Map<String,String> msg=new HashMap<>();
        msg.put("return_code","SUCCESS");
        msg.put("return_msg","OK");

        return msg;

    }
}
