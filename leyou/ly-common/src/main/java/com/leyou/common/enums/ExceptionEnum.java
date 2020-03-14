package com.leyou.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * author:lu
 * create time: 2019/11/24.
 */
@NoArgsConstructor
@AllArgsConstructor
@Getter
public enum ExceptionEnum {
        PRICE_CANNOT_BE_NULL(404,"价格不能为空"),
        CATEGORY_NOT_FOND(404,"商品分类不存在"),
        BRAND_NOT_FOND(404,"品牌不存在"),
        BRAND_SAVE_ERROE(500,"新增商品失败"),
        BRAND_UPDATE_ERROE(500,"修改失败"),
       BRAND_DELETE_ERROE(500,"删除失败"),
       GOODS_NOT_FOND(404,"商品不存在"),
    GOODS_SPU_DETAIL_NOT_FOND(404,"商品详情不存在"),
    GOODS_SPU_ID_NOT_FOND(404,"商品不能为空"),
    GOODS_SUK_NOT_FOND(404,"商品SKU不存在"),
    GOODS_SUK_STOCK_NOT_FOND(404,"商品库存不存在"),
    SPECGROUP_NO_FOND(404,"商品规格组不存在"),
    SPECPRARAM_NOT_FOND(404,"商品规格参数不存在"),
    GOODS_SAVE_ERROR(500,"新增商品失败"),
    GOODS_UPDATE_ERROR(500,"修改商品失败"),
      SPECGROUP_SAVE_ERROE(500,"新增规格组失败"),
    SPECGROUP_UPDATE_ERROE(500,"修改规格组失败"),
    SPECGROUP_DELETE_ERROE(500,"删除规格组失败"),
    SPECPARAM_SAVE_ERROE(500,"新增规格参数失败"),
    SPECPARAM_UPDATE_ERROE(500,"修改规格参数失败"),
    SPECPARAM_DELETE_ERROE(500,"删除规格参数失败"),
    INVALID_USER_DATA_TYPE(400,"用户数据类型无效"),
    INVALID_VERIFY_CODE(400,"无效验证码"),
    USER_NO_FOND(404,"用户不存在"),
    INVALID_VERIFY_PASSWORD(400,"无效密码"),
    RECEIVER_ADDRESS_NOT_FOUND(404,"商品不存在"),
    REACTE_ORDER_ERROE(500,"创建订单失败"),
    STOCK_NOT_ENOUGH(400,"库存不足"),
    ORDER_NOT_FOUND(400,"订单不存在"),
    ORDER_DETAIL_NOT_FOUND(400,"订单详情不存在"),
    ORDER_STATUS_NOT_FOUND(400,"订单状态不存在"),
    ORDER_STATUS_ERROE(500,"订单状态异常"),
    WX_PAY_SIGN_INVALID(500,"签名异常"),
    WX_PAY_NOTIFY_PARAM_ERROR(500,"支付回调返回数据不正确"),
    UPDATE_ORDER_STATUS_ERROR(500,"修改订单状态错误"),
    INVALID_ORDER_PARAM(500,"验证签名与金额失败"),
       ;
        private int code;
        private String msg;
}
