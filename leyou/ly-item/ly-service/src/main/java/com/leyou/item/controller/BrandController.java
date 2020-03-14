package com.leyou.item.controller;

import com.leyou.common.vo.PageResult;
import com.leyou.item.pojo.Brand;
import com.leyou.item.service.BrandService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * author:lu
 * create time: 2019/12/26.
 */

@RestController()
@RequestMapping("/brand")
public class BrandController {

    @Autowired
    private BrandService brandService;

    /**
     * 分页品牌管理
     * @param page
     * @param rows
     * @param sortBy
     * @param desc
     * @param key
     * @return
     */
    @GetMapping("page")
    public ResponseEntity<PageResult<Brand>> queryBrandPage(
            @RequestParam(value = "page",defaultValue = "1") Integer page,
            @RequestParam(value = "rows",defaultValue = "5") Integer rows,
            @RequestParam(value = "sortBy",required = false) String sortBy,
            @RequestParam(value = "desc",defaultValue = "false") boolean desc,
            @RequestParam(value = "key",required = false) String key

    ){
      return ResponseEntity.ok(brandService.queryBrandPage(page,rows,sortBy,desc,key));
    }

    /**
     *新增品牌
     * @param brand
     * @param cids
     * @return
     */
    @PostMapping
    public ResponseEntity<Void> saveBrand(@RequestParam("brand")Brand brand ,@RequestParam("cids") List<Long> cids){
        this.brandService.saveBrand(brand,cids);
              return new ResponseEntity<>(HttpStatus.CREATED);
    }

    /**
     * 修改品牌
     * @param brand
     * @param list
     * @return
     */
    @PutMapping
    public ResponseEntity<Void> updateBrand(Brand brand,@RequestParam("cids") List<Long> list){

        this.brandService.updateBrandand(brand,list);
        return new ResponseEntity<>(HttpStatus.CREATED);
    }

    /**
     * 删除品牌
     * @param bid
     * @return
     */
    @DeleteMapping("bid/{bid}")
    public ResponseEntity<Void> deleteBrand(@PathVariable("bid") Long bid){
        this.brandService.deleteBrand(bid);
        return new ResponseEntity<>(HttpStatus.CREATED);
    }

    /**
     * 根据商品cidc查询品牌集合
     * @param cid
     * @return
     */
    @GetMapping("cid/{cid}")
    public ResponseEntity<List<Brand>> queryBrandByCid(@PathVariable("cid") Long cid){
        return ResponseEntity.ok(this.brandService.queryBrandByCid(cid));
    }

    /**
     * 根据品牌id查询品牌名称
     * @param id
     * @return
     */
    @GetMapping("{id}")
    public ResponseEntity<Brand> queryBrandByid(@PathVariable("id") Long id){
      return ResponseEntity.ok(brandService.queryById(id));
    }

    /**
     * 根据品牌ids集合查询品牌
     * @param ids
     * @return
     */
    @GetMapping("list")
    public ResponseEntity<List<Brand>> queryBrandByIds(@RequestParam("ids") List<Long> ids){
        return ResponseEntity.ok(brandService.queryByIds(ids));
    }
}
