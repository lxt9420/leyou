package com.leyou.cart.controller;

import com.leyou.cart.pojo.Cart;
import com.leyou.cart.service.CartService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * author:lu
 * create time: 2020/3/5.
 */
@Controller
public class CartController {
    @Autowired
    private CartService cartService;
    /**
     * 接收json格式的参数，添加到购物车
     * @param
     * @return
     */
    @PostMapping
    public ResponseEntity<Void> addCart(@RequestBody Cart cart){
        this.cartService.addCart(cart);
        return ResponseEntity.ok().build();
    }
    /**
     * 查询购物车列表
     *
     * @return
     */
    @GetMapping
    public ResponseEntity<List<Cart>> queryCartList(){
        return ResponseEntity.ok(this.cartService.queryCartList());
    }
    /**
     * 更新购物车数量
     * @param skuId
     * @param num
     */
    @PutMapping
    public ResponseEntity<Void> updateNum(
            @RequestParam("skuId")Long skuId,
            @RequestParam("num")Integer num){
        this.cartService.updateNum(skuId,num);
        return ResponseEntity.ok().build();
    }

    /**
     * 通过id删除1条购物车数据
     * @param skuId
     * @return
     */
    @DeleteMapping("{skuId}")
    public ResponseEntity<Void> deleteCart(@PathVariable("skuId")String skuId){
        this.cartService.deleteCart(skuId);
        return ResponseEntity.ok().build();
    }

}
