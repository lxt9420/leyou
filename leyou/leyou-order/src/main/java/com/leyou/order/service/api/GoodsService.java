package com.leyou.order.service.api;


import com.lu.leyou.item.api.GoodsApi;
import org.springframework.cloud.openfeign.FeignClient;

@FeignClient("ly-item-service")
public interface GoodsService extends GoodsApi {
}
