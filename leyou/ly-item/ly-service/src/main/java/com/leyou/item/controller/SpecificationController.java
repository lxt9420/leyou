package com.leyou.item.controller;

import com.leyou.item.pojo.SpecGroup;
import com.leyou.item.pojo.SpecParam;
import com.leyou.item.pojo.Specification;
import com.leyou.item.service.SpecificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * author:lu
 * create time: 2020/1/7.
 */
@RestController
@RequestMapping("spec")
public class SpecificationController {
    @Autowired
    private SpecificationService specificationService;

    @GetMapping("groupjson/{id}")
    public ResponseEntity<String> querySpecificationByCategoryId(@PathVariable("id") Long id){
        Specification spec = this.specificationService.queryById(id);
        if (spec == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        return ResponseEntity.ok(spec.getSpecifications());
    }

    /**
     * 根据分类id查询规格组
     * @param id
     * @return
     */
    @GetMapping("groups/{cid}")
    public ResponseEntity<List<SpecGroup>> queryGroupByCid(@PathVariable("cid")Long id){
        return ResponseEntity.ok(this.specificationService.queryGroupByCid(id));
    }

    /**
     * 添加规格组
     * @param specGroup
     * @return  group
     */
    @PostMapping("group")
    public ResponseEntity<Void> saveSpecGroup(@RequestBody SpecGroup specGroup ){
        this.specificationService.saveSpecGroup(specGroup);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    /**
     * 修改规格组
     * @param specGroup
     * @return
     */
    @PutMapping("group")
    public ResponseEntity<Void> update(@RequestBody SpecGroup specGroup){
        this.specificationService.updateSpecGroup(specGroup);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    /**
     * 删除规格组
     * @param id
     * @return
     */
    @DeleteMapping("group/{id}")
    public ResponseEntity<Void> deleteSpecGroup(@PathVariable("id") Long id){
        this.specificationService.deleteSpecGroup(id);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    /**
     * 查询参数集合
     * @param gid
     * @param cid
     * @param searching
     * @return
     */
   @GetMapping("params")
    public ResponseEntity<List<SpecParam>> queryParamByGid(
            @RequestParam(value = "gid",required = false) Long gid,
            @RequestParam(value = "cid",required = false) Long cid,
            @RequestParam(value = "searching",required = false) Boolean searching

   ){
        return ResponseEntity.ok(this.specificationService.queryParamList(gid,cid,searching));
   }

    /**
     * 新增规格参数
     * @param specParam
     * @return
     */
   @PostMapping("param")
   public ResponseEntity<Void>  saveSpecParam(@RequestBody SpecParam specParam){
       this.specificationService.saveSpecParam(specParam);
       return ResponseEntity.status(HttpStatus.CREATED).build();
   }

    /**
     * 修改规格参数
     * @param specParam
     * @return
     */
   @PutMapping("param")
    public ResponseEntity<Void> updateSpecParam(@RequestBody SpecParam specParam){
     this.specificationService.updateSpecParam(specParam);
       return ResponseEntity.status(HttpStatus.CREATED).build();
   }

    /**
     * 删除规格参数
     * @param id
     * @return
     */
   @DeleteMapping("param/{id}")
    public ResponseEntity<Void> deleteSpecParam(@PathVariable("id") Long id){
      this.specificationService.deleteSpecParam(id);
      return ResponseEntity.status(HttpStatus.CREATED).build();
   }
    /**
     * 根据商品分类id查询规格组
     * @param cid
     * @return
     */
    @GetMapping("group")
    public ResponseEntity<List<SpecGroup>> queryListByCid(@RequestParam("cid") Long cid){
       return ResponseEntity.ok(this.specificationService.queryListByCid(cid));
       // return ResponseEntity.ok(this.specificationService.queryGroupByCid(cid));
    }

}
