package com.leyou.page.web;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import javax.jws.WebParam;

/**
 * author:lu
 * create time: 2020/2/18.
 */
@Controller
public class HelloController {

    @GetMapping("hello")
    public String hello(Model model){
      model.addAttribute("msg","hello,thymeleaf");
        return "hello";
    }
}
