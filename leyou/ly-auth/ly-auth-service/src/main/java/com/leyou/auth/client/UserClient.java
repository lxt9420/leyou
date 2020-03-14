package com.leyou.auth.client;

import com.leyou.user.api.UserApi;
import org.springframework.cloud.openfeign.FeignClient;

/**
 * author:lu
 * create time: 2020/3/3.
 */
@FeignClient("user-service")
public interface UserClient extends UserApi {

}
