package com.leyou.order.client;

import com.lu.leyou.item.api.GoodsApi;
import org.springframework.cloud.openfeign.FeignClient;

/**
 * author:lu
 * create time: 2020/3/9.
 */
@FeignClient("ly-item-service")
public interface GoodsClient extends GoodsApi {
}
