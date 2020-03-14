package com.leyou.cart.service;

import com.leyou.auth.entity.UserInfo;
import com.leyou.cart.client.GoodsClient;
import com.leyou.cart.interceptor.LoginInterceptor;
import com.leyou.cart.pojo.Cart;
import com.leyou.common.utils.JsonUtils;
import com.leyou.item.pojo.Sku;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.BoundHashOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.stream.Collector;
import java.util.stream.Collectors;

/**
 * author:lu
 * create time: 2020/3/5.
 */
@Service
public class CartService {
    @Autowired
    private StringRedisTemplate redisTemplate;
    @Autowired
    private GoodsClient goodsClient;
    static final String KEY_PREFIX = "leyou:cart:uid:";

    static final Logger logger = LoggerFactory.getLogger(CartService.class);
    public void addCart(Cart cart) {
        //获取用户登录信息
        UserInfo user = LoginInterceptor.getLoginUser();
        //存入redis的key
        String key = KEY_PREFIX + user.getId();
        //获取hash操作对象,这一步就是通过key从redis中取出该用户下的所有商品了
        BoundHashOperations<String, Object, Object> hashOps = this.redisTemplate.boundHashOps(key);
        //查询要添加到购物车的商品是否存在于redis
        Long skuId = cart.getSkuId();
        //要加入购物车的商品数量
        Integer num = cart.getNum();
        Boolean boo = hashOps.hasKey(skuId.toString());
        if(boo){
            // 存在，获取购物车数据
            String json = hashOps.get(skuId.toString()).toString();
            //将获取的json字符串格式的商品转化为商品
            cart = JsonUtils.parse(json,Cart.class);
            // 修改购物车数量
            cart.setNum(cart.getNum() + num);
        }else {
            //redis中不存在该购物车信息，则新增购物车
            cart.setUserId(user.getId());
            //其他商品信息，需要查询商品微服务
            Sku sku = this.goodsClient.querySkuById(cart.getSkuId());
            cart.setImage(StringUtils.isBlank(sku.getImages()) ? "" : StringUtils.split(sku.getImages(), ",")[0]);
            cart.setPrice(sku.getPrice());
            cart.setTitle(sku.getTitle());
            cart.setOwnSpec(sku.getOwnSpec());
        }
        //将购物车数据写入redis
        hashOps.put(cart.getSkuId().toString(),JsonUtils.serialize(cart));
    }

    public List<Cart> queryCartList() {
        //获取登录用户
        UserInfo loginUser = LoginInterceptor.getLoginUser();
        String key=KEY_PREFIX + loginUser.getId();
        //判读购物车是否存在
        if(!redisTemplate.hasKey(key)){
           return null;
        }
        BoundHashOperations<String, Object, Object> soob = redisTemplate.boundHashOps(key);
        List<Object> cart = soob.values();
        //判断是否有数据
        if(CollectionUtils.isEmpty(cart)){
            return null;
        }
        //查询购物车数据
        return cart.stream().map(o -> JsonUtils.parse(o.toString(),Cart.class))
                .collect(Collectors.toList());
    }


    public void updateNum(Long skuId, Integer num) {
        //获取用户
        UserInfo loginUser = LoginInterceptor.getLoginUser();
        String key=KEY_PREFIX + loginUser.getId();
        //获取购物车
        BoundHashOperations<String, Object, Object> soob = redisTemplate.boundHashOps(key);
        String json = soob.get(skuId.toString()).toString();
        Cart cart = JsonUtils.parse(json, Cart.class);
        cart.setNum(num);
        //写入购物车
        soob.put(skuId.toString(),JsonUtils.serialize(cart));
    }

    public void deleteCart(String skuId) {
        //获取用户
        UserInfo loginUser = LoginInterceptor.getLoginUser();
        String key=KEY_PREFIX + loginUser.getId();
        //获取购物车
        BoundHashOperations<String, Object, Object> soob = redisTemplate.boundHashOps(key);
        soob.delete(skuId);
    }
}
