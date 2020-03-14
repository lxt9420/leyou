package com.leyou.item.service;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.leyou.common.enums.ExceptionEnum;
import com.leyou.common.exception.LyException;
import com.leyou.common.vo.PageResult;
import com.leyou.item.mapper.BrandMapper;
import com.leyou.item.pojo.Brand;
import com.sun.xml.internal.bind.v2.runtime.unmarshaller.LocatorEx;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.apache.ibatis.annotations.Param;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import tk.mybatis.mapper.entity.Example;

import java.util.List;

/**
 * author:lu
 * create time: 2019/12/26.
 */
@Service
public class BrandService  {
    @Autowired
    private BrandMapper brandMapper;


    public PageResult<Brand> queryBrandPage(Integer page, Integer rows, String sortBy, boolean desc, String key) {
        //分页
        PageHelper.startPage(page,rows);
        //过滤复杂查询使用example
        Example example=  new Example(Brand.class);
        if(StringUtils.isNotBlank(key)){
            example.createCriteria().orLike("name", "%"+key+"%")
                                    .orEqualTo("letter",key.toUpperCase());
        }
        //排序
        if(StringUtils.isNotBlank(sortBy)){
            String orderSortBy=sortBy +" "+ (desc ? "DESC":"ASC");
            example.setOrderByClause(orderSortBy);
        }
        //查询
        List<Brand> brandList = brandMapper.selectByExample(example);
        if(CollectionUtils.isEmpty(brandList)){
            throw new LyException(ExceptionEnum.BRAND_NOT_FOND);
        }
        //分析页面结果
        PageInfo<Brand> brandPageInfo = new PageInfo<>(brandList);
        return new PageResult<>(brandPageInfo.getTotal(),brandList);
    }

    /**
     *新增
     * @param brand
     * @param cids
     */
    @Transactional
    public void saveBrand(Brand brand, List<Long> cids) {
        // 新增品牌信息
        brand.setId(null);
        int count=this.brandMapper.insert(brand);
        if(count !=1){
            throw new LyException(ExceptionEnum.BRAND_SAVE_ERROE);
        }
        // 新增品牌和分类中间表
        for (Long cid : cids) {
           count=this.brandMapper.insertCategoryBrand(cid,brand.getId());
            if(count !=1){
                throw new LyException(ExceptionEnum.BRAND_SAVE_ERROE);
            }
        }
    }

    /**
     * 修改
     * @param brand
     * @param cids
     */
    @Transactional
    public void updateBrandand(Brand brand, List<Long> cids) {

        int count=this.brandMapper.updateByPrimaryKeySelective(brand);
        if(count !=1){
            throw new LyException(ExceptionEnum.BRAND_UPDATE_ERROE);
        }
        //修改品牌和分类中间表
        for (Long cid : cids) {
            int result=this.brandMapper.updateCategoryBrand(cid, brand.getId());
            if(result !=1){
                throw new LyException(ExceptionEnum.BRAND_UPDATE_ERROE);
            }
        }
    }
   /* @Transactional
    public void deleteCategoryBrand(Brand brand,@Param("cids") List<Long> cids){
        int result=this.brandMapper.deleteCategoryBrand(brand.getId());
        if(result!=1){
            throw new LyException(ExceptionEnum.BRAND_DELETE_ERROE);
        }else{
            for (Long cid : cids) {
                int result_insert=this.brandMapper.insertCategoryBrand(cid,brand.getId());
                if(result_insert !=1){
                    throw new LyException(ExceptionEnum.BRAND_UPDATE_ERROE);
                }
            }
        }
    }*/

    /**
     * 删除
     * @param bid
     */
    @Transactional(rollbackFor = Exception.class)
    public void deleteBrand(Long bid) {
        int conut=this.brandMapper.deleteByPrimaryKey(bid);
        if(conut!=1){
            throw new LyException(ExceptionEnum.BRAND_DELETE_ERROE);
        }
        int result=this.brandMapper.deleteCategoryBrand(bid);
        if(result!=1){
            throw new LyException(ExceptionEnum.BRAND_DELETE_ERROE);
        }
    }

    /**
     * 根据id返回品牌
     * @param id
     * @return
     */
    public Brand queryById(Long id){
        Brand brand = brandMapper.selectByPrimaryKey(id);
        if(brand==null){
             throw new LyException(ExceptionEnum.BRAND_NOT_FOND);

        }
        return brand;
    }

    public List<Brand> queryBrandByCid(Long cid) {
        List<Brand> brandList = this.brandMapper.queryByCategoryId(cid);
        if(CollectionUtils.isEmpty(brandList)){
                 throw new LyException(ExceptionEnum.BRAND_NOT_FOND);
        }
        return brandList;
    }

    public List<Brand> queryByIds(List<Long> ids) {
        List<Brand> brandList = this.brandMapper.selectByIdList(ids);
        if(CollectionUtils.isEmpty(brandList)){
            throw new LyException(ExceptionEnum.BRAND_NOT_FOND);
        }
        return brandList;
    }
}
