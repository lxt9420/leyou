package com.leyou.order.service;

import com.leyou.auth.entity.UserInfo;
import com.leyou.common.enums.ExceptionEnum;
import com.leyou.common.exception.LyException;
import com.leyou.common.utils.IdWorker;
import com.leyou.item.pojo.Sku;
import com.leyou.order.client.AddressClient;
import com.leyou.order.client.GoodsClient;
import com.leyou.order.dto.AddressDTO;
import com.leyou.order.dto.OrderDTO;
import com.leyou.order.enums.OrderStatusEnum;
import com.leyou.order.enums.PayState;
import com.leyou.order.interceptor.LoginInterceptor;
import com.leyou.order.mapper.OrderDetailMapper;
import com.leyou.order.mapper.OrderMapper;
import com.leyou.order.mapper.OrderStatusMapper;
import com.leyou.order.pojo.Order;
import com.leyou.order.pojo.OrderDetail;
import com.leyou.order.pojo.OrderStatus;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.util.*;
import java.util.stream.Collectors;
import  com.leyou.order.utils.PayHelper;

/**
 * author:lu
 * create time: 2020/3/9.
 */
@Slf4j
@Service
public class OrderService {
    @Autowired
    private OrderMapper orderMapper;

    @Autowired
    private OrderDetailMapper orderDetailMapper;

    @Autowired
    private OrderStatusMapper orderStatusMapper;

    @Autowired
    private GoodsClient goodsClient;

    @Autowired
    private IdWorker idWorker;
    @Autowired
    private PayHelper payHelper;
   // static final Logger log = LoggerFactory.getLogger(OrderService.class);


   /* @Autowired
    private PayLogService payLogService;*/

    @Transactional
    public Long createOrder(OrderDTO orderDTO) {
    //生成订单ID，采用自己的算法生成订单ID
        long orderId = idWorker.nextId();

        //填充order，订单中的用户信息数据从Token中获取，填充到order中
        Order order = new Order();
        order.setCreateTime(new Date());
        order.setOrderId(orderId);
        order.setPaymentType(orderDTO.getPaymentType());
       // order.setPostFee(0L);  //// TODO 调用物流信息，根据地址计算邮费

        //获取用户信息
        UserInfo user = LoginInterceptor.getLoginUser();
        order.setUserId(user.getId());
        order.setBuyerNick(user.getUsername());
        order.setBuyerRate(false);  //卖家为留言

        //收货人地址信息，应该从数据库中物流信息中获取，这里使用的是假的数据
        AddressDTO addressDTO = AddressClient.findById(orderDTO.getAddressId());
        if (addressDTO == null) {
            // 商品不存在，抛出异常
            throw new LyException(ExceptionEnum.RECEIVER_ADDRESS_NOT_FOUND);
        }
        order.setReceiver(addressDTO.getName());
        order.setReceiverAddress(addressDTO.getAddress());
        order.setReceiverCity(addressDTO.getCity());
        order.setReceiverDistrict(addressDTO.getDistrict());
        order.setReceiverMobile(addressDTO.getPhone());
        order.setReceiverZip(addressDTO.getZipCode());
        order.setReceiverState(addressDTO.getState());


        //付款金额相关，首先把orderDto转化成map，其中key为skuId,值为购物车中该sku的购买数量
        Map<Long, Integer> skuNumMap = orderDTO.getCarts().stream()
                .collect(Collectors.toMap(c -> c.getSkuId(), c -> c.getNum()));

        //查询商品信息，根据skuIds批量查询sku详情
        Set<Long> ids = skuNumMap.keySet();
        List<Sku> skus = goodsClient.querySkuByIds(new ArrayList<>(ids));
        if (CollectionUtils.isEmpty(skus)) {
            throw new LyException(ExceptionEnum.GOODS_NOT_FOND);
        }
        Long totalPay = 0L;
        //填充orderDetail
        ArrayList<OrderDetail> orderDetails = new ArrayList<>();

        //遍历skus，填充orderDetail
        for (Sku sku : skus) {
            totalPay += skuNumMap.get(sku.getId()) * sku.getPrice();

            OrderDetail orderDetail = new OrderDetail();
            orderDetail.setImage(StringUtils.substringBefore(sku.getImages(), ","));
            orderDetail.setNum(skuNumMap.get(sku.getId()));
            orderDetail.setOrderId(orderId);
            orderDetail.setOwnSpec(sku.getOwnSpec());
            orderDetail.setPrice(sku.getPrice());
            orderDetail.setSkuId(sku.getId());
            orderDetail.setTitle(sku.getTitle());
            orderDetails.add(orderDetail);
        }

        order.setActualPay(totalPay + order.getPostFee()-0);
        order.setTotalPay(totalPay);

        //保存order
        int count=orderMapper.insertSelective(order);
        if (count!=1) {
           log.error("[订单创建]订单创建失败，orderId:{}",orderId);
            throw new LyException(ExceptionEnum.REACTE_ORDER_ERROE);
        }
        //保存detail
        count=orderDetailMapper.insertList(orderDetails);
        if (count!=1) {
            log.error("[订单创建]订单创建失败，orderId:{}",orderId);
            throw new LyException(ExceptionEnum.REACTE_ORDER_ERROE);
        }

        //填充orderStatus
        OrderStatus orderStatus = new OrderStatus();
        orderStatus.setOrderId(orderId);
        orderStatus.setStatus(OrderStatusEnum.UN_PAY.value());
        orderStatus.setCreateTime(order.getCreateTime());

        //保存orderStatus
        count=orderStatusMapper.insertSelective(orderStatus);
        if (count!=1) {
            log.error("[订单创建]订单创建失败，orderId:{}",orderId);
            throw new LyException(ExceptionEnum.REACTE_ORDER_ERROE);
        }
        //减库存
        goodsClient.decreaseStock(orderDTO.getCarts());
        return orderId;
    }

    public Order queryOrderById(Long id) {
        //根据订单ID查询订单
        Order order = orderMapper.selectByPrimaryKey(id);
        //判断订单状态
        if (order==null) {
            throw new LyException(ExceptionEnum.ORDER_NOT_FOUND);
        }
       //查询订单详情
       OrderDetail detail=new OrderDetail();
       detail.setOrderId(id);
        List<OrderDetail> details = orderDetailMapper.select(detail);
        if (CollectionUtils.isEmpty(details)) {
            throw new LyException(ExceptionEnum.ORDER_DETAIL_NOT_FOUND);
        }
        order.setOrderDetails(details);
        //查看订单状态
        OrderStatus orderStatus = orderStatusMapper.selectByPrimaryKey(id);
        if(orderStatus==null){
            throw new LyException(ExceptionEnum.ORDER_STATUS_NOT_FOUND);
        }
        order.setOrderStatus(orderStatus);
        return order;
    }

    public String createPayUrl(Long orderId) {
        //根据订单ID查询订单
        Order order = queryOrderById(orderId);
        //判断订单状态
        Integer status = order.getOrderStatus().getStatus();
        if (status!=OrderStatusEnum.UN_PAY.value()) {
            //订单状态异常
            throw new LyException(ExceptionEnum.ORDER_STATUS_ERROE);
        }
        Long actualPay =1L;/* order.getActualPay();*/
        OrderDetail detail = order.getOrderDetails().get(0);
        String desc = detail.getTitle();
        return payHelper.createOrder(orderId,actualPay,desc);
    }
    /**
     * 处理回调结果
     *
     * @param result
     */
    public void handleNotify(Map<String,String> result){
        //数据校验
        payHelper.isSuccess(result);
        //检验签名
        payHelper.isValidSign(result);

        //检验金额
        //解析数据
        String totalFeeStr = result.get("total_fee");  //订单金额
        String outTradeNo = result.get("out_trade_no");  //订单编号
       // String transactionId = msg.get("transaction_id");  //商户订单号
        //String bankType = msg.get("bank_type");  //银行类型
        if (StringUtils.isEmpty(totalFeeStr) || StringUtils.isEmpty(outTradeNo)) {
            log.error("【微信支付回调】支付回调返回数据不正确");
            throw new LyException(ExceptionEnum.WX_PAY_NOTIFY_PARAM_ERROR);
        }
        //获取结果的金额
        Long totalFee = Long.valueOf(totalFeeStr);
        //获取订单金额
        Long orderId=Long.valueOf(outTradeNo);
        //查询订单
        Order order = orderMapper.selectByPrimaryKey(orderId);

        //todo 这里验证回调数据时，支付金额使用1分进行验证，后续使用实际支付金额验证
        if (/*order.getActualPay()*/1 != totalFee) {
            log.error("【微信支付回调】支付回调返回数据不正确");
            throw new LyException(ExceptionEnum.WX_PAY_NOTIFY_PARAM_ERROR);

        }

        //修改订单状态
        OrderStatus status = new OrderStatus();
        status.setStatus(OrderStatusEnum.PAY_UP.value());
        status.setOrderId(orderId);
        status.setPaymentTime(new Date());
        int count = orderStatusMapper.updateByPrimaryKeySelective(status);
        if (count!=1) {
            throw new LyException(ExceptionEnum.UPDATE_ORDER_STATUS_ERROR);
        }
        log.info("【订单回调】订单支付成功！订单编号：{}",orderId);
    }

    public PayState queryOrderState(Long orderId) {
        OrderStatus orderStatus = orderStatusMapper.selectByPrimaryKey(orderId);
        Integer status = orderStatus.getStatus();
        if(status!=OrderStatusEnum.UN_PAY.value()){
            return PayState.SUCCESS;
        }
        return payHelper.queryPayState(orderId);
    }
}
