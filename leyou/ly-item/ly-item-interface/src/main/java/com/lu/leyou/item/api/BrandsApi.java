package com.lu.leyou.item.api;

import com.leyou.item.pojo.Brand;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

/**
 * author:lu
 * create time: 2020/2/14.
 */
public interface BrandsApi {
    /**
     * 根据品牌id查询品牌名称
     * @param id
     * @return
     */
    @GetMapping("brand/{id}")
    public Brand queryBrandByid(@PathVariable("id") Long id);
    /**
     * 根据品牌ids集合查询品牌
     * @param ids
     * @return
     */
    @GetMapping("brand/list")
    public List<Brand> queryBrandByIds(@RequestParam("ids") List<Long> ids);

}
