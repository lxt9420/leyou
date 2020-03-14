package com.leyou.item.controller;

import com.leyou.common.dto.CartDTO;
import com.leyou.common.vo.PageResult;
import com.leyou.item.pojo.Sku;
import com.leyou.item.pojo.Spu;
import com.leyou.item.pojo.SpuDetail;
import com.leyou.item.service.GoodsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * author:lu
 * create time: 2020/1/12.
 */
@RestController
public class GoodsController {
    @Autowired
    private GoodsService goodsService;

    /**
     * 商品分页列表
     * @param page
     * @param rows
     * @param saleable
     * @param key
     * @return
     */
    @RequestMapping("spu/page")
    public ResponseEntity<PageResult<Spu>> querySpuByPage(
            @RequestParam(value = "page",defaultValue = "1") Integer page,
            @RequestParam(value="rows",defaultValue = "5")  Integer rows,
            @RequestParam(value = "saleable",required = false) Boolean saleable,
            @RequestParam(value = "key" ,required = false) String key
    ){
      return ResponseEntity.ok(goodsService.querySpuByPage(page,rows,saleable,key));
    }

    /**
     * 新增商品
     * @param spu
     * @return
     */
    @PostMapping("goods")
    public ResponseEntity<Void> saveGoods(@RequestBody Spu spu){
        this.goodsService.saveGoods(spu);
        return ResponseEntity.status(HttpStatus.CREATED).build();

    }

    /**
     * 根据spu的id查询商品详情  http://api.leyou.com/api/item/spu/detail/168
     * @param spuId
     * @return
     */
    @GetMapping("/spu/detail/{id}")
    public ResponseEntity<SpuDetail> queyrDetailById(@PathVariable("id") Long spuId){
        return ResponseEntity.ok(goodsService.queyrDetailById(spuId));
    }

    /**
     * 查询spu下的所有sku   http://api.leyou.com/api/item/sku/list?id=168
     * @param spuId
     * @return
     */
    @GetMapping("sku/list")
    public ResponseEntity<List<Sku>> querySkuByListId(@RequestParam("id") Long spuId){
              return ResponseEntity.ok(this.goodsService.querySkuByListId(spuId));
    }
    /**
     * 查询批量id集合所有sku
     * @param ids
     * @return
     */
    @GetMapping("sku/list/ids")
    public ResponseEntity<List<Sku>> querySkuByIds(@RequestParam("ids") List<Long> ids){
        return ResponseEntity.ok(this.goodsService.querySkuByIds(ids));
    }
    /**
     * 修改商品
     * @param spu
     * @return
     */
    @PutMapping("goods")
    public ResponseEntity<Void> updateGoods(@RequestBody Spu spu){
        this.goodsService.updateGoods(spu);
        return ResponseEntity.status(HttpStatus.CREATED).build();

    }
    /**
     * 根据spu的id查询spu
     * @param id
     * @return
     */
    @GetMapping("spu/{id}")
    public ResponseEntity<Spu> querySpuById(@PathVariable("id") Long id){
        return ResponseEntity.ok(goodsService.querySpuById(id));
    }
    /**
     * 通过sku的id查询sku
     * @param id
     * @return
     */
    @GetMapping("sku/{id}")
    public ResponseEntity<Sku> querySkuById(@PathVariable("id") Long id){
        return ResponseEntity.ok(goodsService.querySkuById(id));
    }

    /**
     * 减库存
     * @param carts
     * @return
     */
    @PostMapping("stock/decrease")
    public ResponseEntity<Void> decreaseStock(@RequestBody List<CartDTO> carts){
        goodsService.decreaseStock(carts);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
}
