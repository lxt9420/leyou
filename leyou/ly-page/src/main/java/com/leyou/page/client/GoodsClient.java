package com.leyou.page.client;

import com.lu.leyou.item.api.GoodsApi;
import org.springframework.cloud.openfeign.FeignClient;

/**
 * author:lu
 * create time: 2020/2/14.
 */
@FeignClient("LY-ITEM-SERVICE")
public interface GoodsClient extends GoodsApi {
}
