package com.leyou.common.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * author:lu
 * create time: 2020/3/9.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CartDTO {
    private Long skuId;
    private Integer num;
}
