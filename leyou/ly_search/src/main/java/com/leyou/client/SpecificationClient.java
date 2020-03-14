package com.leyou.client;

import com.lu.leyou.item.api.SpecificationApi;
import org.springframework.cloud.openfeign.FeignClient;

/**
 * author:lu
 * create time: 2020/2/14.
 */
@FeignClient("LY-ITEM-SERVICE")
public interface SpecificationClient extends SpecificationApi {
}
