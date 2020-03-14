package com.leyou.user.api;

import com.leyou.user.pojo.User;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * author:lu
 * create time: 2020/3/3.
 */
public interface UserApi {
    @GetMapping("query")
    User queryUserByUsernameAndPassword(
            @RequestParam("userName")String username,
            @RequestParam("passWord")String password);
}
