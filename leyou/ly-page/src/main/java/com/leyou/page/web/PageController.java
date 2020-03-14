package com.leyou.page.web;

import com.leyou.page.service.PageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * author:lu
 * create time: 2020/2/18.
 */
@Controller
public class PageController {

   @Autowired
   private PageService pageService;
    /**
     * 跳转到商品详情页
     * @return
     */
    @GetMapping("item/{id}.html")
    public String toItemPage(@PathVariable("id") Long spuId,Model model){
     //查询数据模型
     Map<String,Object> attributes=pageService.loadModel(spuId);
     //准备模型数据
    model.addAllAttributes(attributes);
     //返回视图
     return "item";
    }
}
