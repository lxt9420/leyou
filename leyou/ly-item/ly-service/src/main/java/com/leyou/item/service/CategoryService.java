package com.leyou.item.service;


import com.leyou.common.enums.ExceptionEnum;
import com.leyou.common.exception.LyException;
import com.leyou.item.mapper.CategoryMapper;
import com.leyou.item.pojo.Category;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.List;

/**
 * author:lu
 * create time: 2019/12/17.
 */
@Service
public class CategoryService {

    @Autowired
    private CategoryMapper categoryMapper;


    public List<Category> queryCategoryListByPid(Long pid) {
        Category category = new Category();
        category.setParentId(pid);
        List<Category> categoryList = categoryMapper.select(category);
        if(CollectionUtils.isEmpty(categoryList)){
           throw new LyException(ExceptionEnum.CATEGORY_NOT_FOND);
        }
        return categoryList;
    }
    public List<Category> queryByBrandId(Long bid) {
        return this.categoryMapper.queryByBrandId(bid);
    }

    /**
     * 根据集合id查询商品分类名称
     * @param ids
     * @return
     */
    public List<Category> queryById(List<Long> ids){
        List<Category> categories = categoryMapper.selectByIdList(ids);
        if(CollectionUtils.isEmpty(categories)){
         throw new LyException(ExceptionEnum.CATEGORY_NOT_FOND);
        }
        return categories;

    }


}
