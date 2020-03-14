package com.leyou.order.utils;

import com.github.wxpay.sdk.WXPay;

import com.github.wxpay.sdk.WXPayConstants;
import com.github.wxpay.sdk.WXPayUtil;
import com.leyou.common.enums.ExceptionEnum;
import com.leyou.common.exception.LyException;
import com.leyou.order.config.PayConfig;
import com.leyou.order.enums.OrderStatusEnum;
import com.leyou.order.enums.PayState;
import com.leyou.order.mapper.OrderMapper;
import com.leyou.order.mapper.OrderStatusMapper;
import com.leyou.order.pojo.Order;
import com.leyou.order.pojo.OrderStatus;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import static com.github.wxpay.sdk.WXPayConstants.*;

/**
 * @author bystander
 * @date 2018/10/5
 */
@Slf4j
@Component
public class PayHelper {

    @Autowired
    private WXPay wxPay;

    @Autowired
    private PayConfig config;

    @Autowired
    private OrderMapper orderMapper;

    @Autowired
    private OrderStatusMapper statusMapper;


    public String createOrder(Long orderId, Long totalPay, String desc) {
        try {
            Map<String, String> data = new HashMap<>();
            //描述
            data.put("body", desc);
            //订单号
            data.put("out_trade_no", orderId.toString());
            //货币（默认就是人民币）
            //data.put("fee_type", "CNY");
            //总金额
            data.put("total_fee", totalPay.toString());
            //调用微信支付的终端ip
            data.put("spbill_create_ip", "127.0.0.1");
            //回调地址
            data.put("notify_url",config.getNotifyUrl() );
            //交易类型为扫码支付
            data.put("trade_type", "NATIVE");
            //利用wxPay工具，完成下单
            Map<String, String> result = wxPay.unifiedOrder(data);
            //判断通信提示
            isSuccess(result);
            //下单成功，获取支付连接
            String url = result.get("code_url");
            return url;
        } catch (Exception e) {
            log.error("【微信下单】创建预交易订单异常", e);
            return null;
        }
    }

    public void isSuccess(Map<String,String> result) {
        //判断通信提示
        String returnCode = result.get("return_code");
        if (FAIL.equals(returnCode)) {
            log.error("【微信下单】与微信通信失败，失败信息：{}", result.get("return_msg"));
        }
        //判断业务提示
        if (FAIL.equals(result.get("result_code"))) {
            log.error("【微信下单】创建预交易订单失败，错误码：{}，错误信息：{}",
                    result.get("err_code"), result.get("err_code_des"));
        }
    }

    /**
     * 检验签名
     *
     * @param data
     */
    public void isValidSign(Map<String, String> data) {
        try {
            String sing1 = WXPayUtil.generateSignature(data, config.getKey(), WXPayConstants.SignType.HMACSHA256);
            String sing2 = WXPayUtil.generateSignature(data, config.getKey(), WXPayConstants.SignType.MD5);
            String sign = data.get("sign");
            if (!StringUtils.equals(sign,sing1)&&!StringUtils.equals(sign,sing2)) {
                //签名异常
                throw new LyException(ExceptionEnum.WX_PAY_SIGN_INVALID);
            }
        } catch (Exception e) {
            log.error("【微信支付】检验签名失败，数据：{}", data);
            throw new LyException(ExceptionEnum.WX_PAY_SIGN_INVALID);
        }
    }


    /**
     * 查询订单支付状态
     *
     * @param orderId
     * @return
     */
    public PayState queryPayState(Long orderId) {
        try {
            Map<String, String> data = new HashMap<>();
            data.put("out_trade_no", orderId.toString());

            Map<String, String> result = wxPay.orderQuery(data);
            //校验状态
            isSuccess(result);
            //检验签名
            isValidSign(result);
            String totalFeeStr = result.get("total_fee");
            String tradeNo = result.get("out_trade_no");
            if(StringUtils.isEmpty(totalFeeStr)||StringUtils.isEmpty(tradeNo)){
                throw new LyException(ExceptionEnum.INVALID_ORDER_PARAM);
            }
            //获取结果金额
            Long tatalFee = Long.valueOf(totalFeeStr);
            //获取订单金额
            Order order = orderMapper.selectByPrimaryKey(orderId);
            if(tatalFee!=1){
                throw new LyException(ExceptionEnum.INVALID_ORDER_PARAM);
            }

            //查询支付状态
            String state = result.get("trade_state");
            if (StringUtils.equals("SUCCESS", state)) {
                //支付成功, 修改支付状态等信息
                //修改订单状态
                OrderStatus status = new OrderStatus();
                status.setStatus(OrderStatusEnum.PAY_UP.value());
                status.setOrderId(orderId);
                status.setPaymentTime(new Date());
                int count = statusMapper.updateByPrimaryKeySelective(status);
                if (count!=1) {
                    throw new LyException(ExceptionEnum.UPDATE_ORDER_STATUS_ERROR);
                }
                return PayState.SUCCESS;
            }

            if (StringUtils.equals("USERPAYING", state) || StringUtils.equals("NOTPAY", state)) {
                //未支付成功
                return PayState.NOT_PAY;
            }
            //其他返回付款失败
            return PayState.FAIL;
        } catch (Exception e) {
            log.error("查询订单支付状态异常", e);
            return PayState.NOT_PAY;
        }


    }
}
