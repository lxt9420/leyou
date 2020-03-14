package com.leyou.item.mapper;

import com.leyou.common.mapper.BaseMapper;
import com.leyou.item.pojo.Brand;
import org.apache.ibatis.annotations.*;
import tk.mybatis.mapper.common.Mapper;

import java.util.List;

/**
 * author:lu
 * create time: 2019/12/26.
 */
public interface BrandMapper extends BaseMapper<Brand> {
    @Insert("INSERT INTO tb_category_brand (category_id,brand_id) VALUES(#{cid},#{bid})")
    int insertCategoryBrand(@Param("cid") Long cid,@Param("bid") Long bid);
    @Delete("delete from tb_category_brand where brand_id=#{bid}")
    int deleteCategoryBrand(@Param("bid") Long bid);
    @Select("select category_id,brand_id from tb_category_brand where brand_id=#{bid}")
    int selectCategoryBrand(@Param("bid") Long bid);
    /**
     * 修改商品分类和品牌中间表数据
     * @param cid
     * @param bid
     */
    @Update("update tb_category_brand set category_id = #{cid} where brand_id = #{bid}")
    Integer updateCategoryBrand(@Param("cid") Long cid, @Param("bid") Long bid);
    @Select("SELECT b.* FROM tb_brand b LEFT JOIN tb_category_brand cb ON b.id = cb.brand_id WHERE cb.category_id = #{cid}")
    List<Brand> queryByCategoryId(Long cid);

}
