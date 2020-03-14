package com.leyou.page.service;

import com.leyou.item.pojo.*;
import com.leyou.page.client.BrandsClient;
import com.leyou.page.client.CategoryClient;
import com.leyou.page.client.GoodsClient;
import com.leyou.page.client.SpecificationClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.io.File;
import java.io.PrintWriter;
import java.util.*;

/**
 * author:lu
 * create time: 2020/2/19.
 */
@Slf4j
@Service
public class PageService {
    @Autowired
    private BrandsClient brandsClient;
    @Autowired
    private CategoryClient categoryClient;
    @Autowired
    private GoodsClient goodsClient;
    @Autowired
    private SpecificationClient specificationClient;
    @Autowired
    private TemplateEngine templateEngine;

    public Map<String,Object> loadModel(Long spuId) {
    Map<String,Object> model= new HashMap<>();
    //查询spu
        Spu spu = goodsClient.querySpuById(spuId);
        //查询sku
        List<Sku> skus = spu.getSkus();
        //查询dettail
        SpuDetail spuDetail = spu.getSpuDetail();
        //查询brand
        Brand brand = brandsClient.queryBrandByid(spu.getBrandId());
        // 查询商品分类category
        List<Category> categories = categoryClient.
                queryCategoryByIds(Arrays.asList(spu.getCid1(), spu.getCid2(), spu.getCid3()));
        //查询规格参数
        List<SpecGroup> groups = this.specificationClient
                .queryGroupByCid(spu.getCid3());
        // 查询特殊的规格参数
        List<SpecParam> params = this.specificationClient
         .queryParamByGid(null, spu.getCid3(), false);
        Map<Long, String> paramMap = new HashMap<>();
        params.forEach(param -> {
            paramMap.put(param.getId(), param.getName());
        });
        // 封装spu
        model.put("spu", spu);
        // 封装spuDetail
        model.put("spuDetail", spuDetail);
        // 封装sku集合
        model.put("skus", skus);
        // 分类
        model.put("categories", categories);
        // 品牌
        model.put("brand", brand);
        // 规格参数组
        model.put("groups", groups);
        // 查询特殊规格参数
        model.put("paramMap", paramMap);
        return model;
    }
    public void createHtml(Long spuId){
        //上下文
        Context context=new Context();
        context.setVariables(loadModel(spuId));
        //输出流
        File dest=new File("D:\\project\\item\\",spuId+".html");
        if(dest.exists()){
            dest.delete();
        }
        try(PrintWriter writer=new PrintWriter(dest,"UTF-8")){
            templateEngine.process("item",context,writer);

        }catch (Exception e){
         log.error("静态服务页面生成异常"+e);
        }


    }

    public void deleteHtml(Long spuId) {
        File dest=new File("D:\\project\\item\\",spuId+".html");
        if(dest.exists()){
            dest.delete();
        }
    }
}
