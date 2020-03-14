package com.lu.leyou.item.api;

import com.leyou.item.pojo.SpecGroup;
import com.leyou.item.pojo.SpecParam;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

/**
 * author:lu
 * create time: 2020/2/14.
 */
public interface SpecificationApi {
    /**
     * 查询参数集合
     * @param gid
     * @param cid
     * @param searching
     * @return
     */
    @GetMapping("spec/params")
    public List<SpecParam> queryParamByGid(
            @RequestParam(value = "gid",required = false) Long gid,
            @RequestParam(value = "cid",required = false) Long cid,
            @RequestParam(value = "searching",required = false) Boolean searching

    );

    /**
     * 根据商品分类id查询规格组
     * @param cid
     * @return
     */
    @GetMapping("spec/group")
    public List<SpecGroup> queryGroupByCid(@RequestParam("cid") Long cid);


}
