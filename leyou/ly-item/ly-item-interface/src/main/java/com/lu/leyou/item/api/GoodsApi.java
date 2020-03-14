package com.lu.leyou.item.api;

import com.leyou.common.dto.CartDTO;
import com.leyou.common.vo.PageResult;
import com.leyou.item.pojo.Sku;
import com.leyou.item.pojo.Spu;
import com.leyou.item.pojo.SpuDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * author:lu
 * create time: 2020/2/14.
 */
public interface  GoodsApi {
    /**
     * 商品分页列表
     * @param page
     * @param rows
     * @param saleable
     * @param key
     * @return
     */
    @RequestMapping("spu/page")
    public PageResult<Spu> querySpuByPage(
            @RequestParam(value = "page",defaultValue = "1") Integer page,
            @RequestParam(value="rows",defaultValue = "5")  Integer rows,
            @RequestParam(value = "saleable",required = false) Boolean saleable,
            @RequestParam(value = "key" ,required = false) String key
    );
    /**
     * 根据spu的id查询商品详情  http://api.leyou.com/api/item/spu/detail/168
     * @param spuId
     * @return
     */
    @GetMapping("/spu/detail/{id}")
     SpuDetail queyrDetailById(@PathVariable("id") Long spuId);
    /**
     * 查询spu下的所有sku   http://api.leyou.com/api/item/sku/list?id=168
     * @param spuId
     * @return
     */
    @GetMapping("sku/list")
     List<Sku> querySkuByListId(@RequestParam("id") Long spuId);
    /**
     * 查询批量id集合所有sku
     * @param ids
     * @return
     */
    @GetMapping("sku/list/ids")
    List<Sku> querySkuByIds(@RequestParam("ids") List<Long> ids);
    /**
     * 根据id查询spu
     * @param id
     * @return
     */
    @GetMapping("spu/{id}")
     Spu querySpuById(@PathVariable("id") Long id);
    /**
     * 通过sku的id查询sku
     * @param id
     * @return
     */
    @GetMapping("sku/{id}")
    Sku querySkuById(@PathVariable("id") Long id);
    /**
     * 减库存
     * @param carts
     * @return
     */
    @PostMapping("stock/decrease")
    public ResponseEntity<Void> decreaseStock(@RequestBody List<CartDTO> carts);
}
