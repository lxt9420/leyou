package com.leyou.user.controller;

import com.leyou.user.pojo.User;
import com.leyou.user.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

/**
 * author:lu
 * create time: 2020/2/28.
 */
@RestController
public class UserController {
    @Autowired
    private UserService userService;

    /**
     * 检验数据
     * @param data
     * @param type
     * @return
     */
    @GetMapping("/check/{data}/{type}")
    public ResponseEntity<Boolean> checkData(@PathVariable("data")String data,@PathVariable("type")Integer type){
           return ResponseEntity.ok(userService.checkData(data,type));
    }

    /**
     * 发送短信
     * @param phone
     * @return
     */
    @PostMapping("code")
    public ResponseEntity<Void> sendCode(@RequestParam("phone") String phone){
        userService.sendCode(phone);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    /**
     * 注册
     * @param code
     * @return
     */
    @PostMapping("register")
    public ResponseEntity<Void> register(@Valid User user,@RequestParam("code")String code){
        userService.register(user,code);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }
    @GetMapping("query")
    public ResponseEntity<User> queryUserByUsernameAndPassword(
            @RequestParam("userName")String username,
            @RequestParam("passWord")String password){
       return ResponseEntity.ok(userService.queryUserByUsernameAndPassword(username,password));
    }
}
