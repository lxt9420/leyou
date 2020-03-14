package com.leyou.client;

import com.leyou.item.pojo.Category;
import com.lu.leyou.item.api.CategoryApi;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

/**
 * author:lu
 * create time: 2020/2/14.
 */
@FeignClient("LY-ITEM-SERVICE")
public interface CategoryClient extends CategoryApi  {
}
