package com.leyou.item.service;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.leyou.common.dto.CartDTO;
import com.leyou.common.enums.ExceptionEnum;
import com.leyou.common.exception.LyException;
import com.leyou.common.vo.PageResult;
import com.leyou.item.mapper.SkuMapper;
import com.leyou.item.mapper.SpuDetailMapper;
import com.leyou.item.mapper.SpuMapper;
import com.leyou.item.mapper.StockMapper;
import com.leyou.item.pojo.*;
import org.apache.commons.lang3.StringUtils;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import tk.mybatis.mapper.entity.Example;

import java.util.*;
import java.util.stream.Collectors;

/**
 * author:lu
 * create time: 2020/1/12.
 */
@Service
public class GoodsService {

    @Autowired
    private SpuMapper spuMapper;
    @Autowired
    private SpuDetailMapper spuDetailMapper;
    @Autowired
    private CategoryService categoryService;
    @Autowired
    private BrandService brandService;
    @Autowired
    private SkuMapper skuMapper;
    @Autowired
    private StockMapper stockMapper;
    @Autowired
    private AmqpTemplate amqpTemplate;

    public PageResult<Spu> querySpuByPage(Integer page, Integer rows, Boolean saleable, String key) {
        PageHelper.startPage(page,rows);

        Example example = new Example(Spu.class);
        Example.Criteria criteria = example.createCriteria();
        //过滤
        if(StringUtils.isNotBlank(key)){
             criteria.andLike("title","%"+key+"%");
        }
        //判断是否上架
        if(saleable !=null){
            criteria.andEqualTo("saleable",saleable);
        }
        //排序
        example.setOrderByClause("last_update_time DESC");
        //查询
        List<Spu> spus = spuMapper.selectByExample(example);
        if(CollectionUtils.isEmpty(spus)){
            throw new LyException(ExceptionEnum.GOODS_NOT_FOND);
        }
        //解析商品分类和品牌名称
        loadCategoryAndBrandName(spus);
        PageInfo<Spu> info=new PageInfo<>(spus);
        return new PageResult<>(info.getTotal(),spus);
    }

    /**
     * 解析商品分类和品牌名称
     * @param spus
     */
    private void loadCategoryAndBrandName(List<Spu> spus) {
        for(Spu spu : spus){
             //处理商品分类名称
         List<String>  names=  categoryService.queryById(Arrays.asList(spu.getCid1(),spu.getCid2(),spu.getCid3()))
                    .stream().map(Category::getName).collect(Collectors.toList());
         spu.setCname(StringUtils.join(names,"/"));
         spu.setBname(brandService.queryById(spu.getBrandId()).getName());
        }
    }

    @Transactional
    public void saveGoods(Spu spu) {
        //新增spu
        spu.setId(null);
        spu.setCreateTime(new Date());
        spu.setLastUpdateTime(spu.getCreateTime());
        spu.setSaleable(true);
        spu.setValid(false);
        int count = spuMapper.insert(spu);
        if(count!=1){
            throw new LyException(ExceptionEnum.GOODS_SAVE_ERROR);
        }
        //新增spuDetail
        SpuDetail spuDetail =spu.getSpuDetail();
        spuDetail.setSpuId(spu.getId());
        spuDetailMapper.insert(spuDetail);
        //新增sku和stock
        saveSkuAndStock(spu);
        //发送信息
        amqpTemplate.convertAndSend("item.insert",spu.getId());
    }

    public SpuDetail queyrDetailById(Long spuId) {
        SpuDetail spuDetail = this.spuDetailMapper.selectByPrimaryKey(spuId);
        if(spuDetail==null){
            throw new LyException(ExceptionEnum.GOODS_SPU_DETAIL_NOT_FOND);
        }
        return spuDetail;
    }

    public List<Sku> querySkuByListId(Long spuId) {
        Sku sku=new Sku();
        sku.setSpuId(spuId);
        List<Sku> skuList = this.skuMapper.select(sku);
        if(CollectionUtils.isEmpty(skuList)){
            throw new LyException(ExceptionEnum.GOODS_SUK_NOT_FOND);
        }
        List<Long> collect = skuList.stream().map(Sku::getId).collect(Collectors.toList());
        querySkuToStock(skuList, collect);

        return skuList;
    }

    private void querySkuToStock(List<Sku> skuList, List<Long> collect) {
        //查询库存
        List<Stock> stockList = this.stockMapper.selectByIdList(collect);
        if(CollectionUtils.isEmpty(stockList)){
            throw new LyException(ExceptionEnum.GOODS_SUK_STOCK_NOT_FOND);
        }
        //---------------------------------------------
        //下面的stockMap和skuList.forEach是想把stock的库存放到skuList的库存字段。如：要用两个for循环才能放入。但是这样影响速度，所以用stockMap和skuList.forEach

         /*for (Sku sku1 : skuList) {
            for (Stock stock : stockList) {
                if(sku1.getId()==stock.getSkuId()){
                    sku1.setStock(stock.getStock());
                }
            }
        }*/
        //---------------------------------------------
        //把stock变成一个map,key是sku的id，value是库存

        Map<Long, Integer> stockMap = stockList.stream()
                .collect(Collectors.toMap(Stock::getSkuId, Stock::getStock));
        skuList.forEach(s->s.setStock(stockMap.get(s.getId())));
    }

    @Transactional
    public void updateGoods(Spu spu) {
        if(spu.getId()==null){
            throw new LyException(ExceptionEnum.GOODS_SPU_ID_NOT_FOND);
        }
        Sku sku=new Sku();
        sku.setSpuId(spu.getId());
        List<Sku> skus = this.skuMapper.select(sku);
        if(!CollectionUtils.isEmpty(skus)){
            //删除sku
            skuMapper.delete(sku);
            //删除stock
            List<Long> ids = skus.stream().map(Sku::getId).collect(Collectors.toList());
            stockMapper.selectByIdList(ids);
        }
        spu.setValid(null);
        spu.setSaleable(null);
        spu.setLastUpdateTime(new Date());
        spu.setCreateTime(null);
        int count = spuMapper.updateByPrimaryKeySelective(spu);
        if(count!=1){
                throw new LyException(ExceptionEnum.GOODS_UPDATE_ERROR);
        }
        //修改detail
        count  = spuDetailMapper.updateByPrimaryKeySelective(spu.getSpuDetail());
        if(count!=1){
            throw new LyException(ExceptionEnum.GOODS_UPDATE_ERROR);
        }
        //新增sku和stock
        saveSkuAndStock(spu);
        //发送mq消息
        amqpTemplate.convertAndSend("item.update",spu.getId());
    }
    private void saveSkuAndStock(Spu spu){
        int count;
        //新增sku
        List<Stock> stockList=new ArrayList<>();
        List<Sku> skus=spu.getSkus();
        for (Sku sku : skus) {
            sku.setSpuId(spu.getId());
            sku.setCreateTime(new Date());
            sku.setLastUpdateTime(sku.getCreateTime());
            count=skuMapper.insert(sku);
            if(count!=1){
                throw new LyException(ExceptionEnum.GOODS_SAVE_ERROR);
            }
            Stock stock=new Stock();
            stock.setSkuId(sku.getId());
            stock.setStock(sku.getStock());
            stockList.add(stock);
        }
        //新增stock
        count=stockMapper.insertList(stockList);
        if(count!=stockList.size()){
            throw new LyException(ExceptionEnum.GOODS_SAVE_ERROR);
        }
    }

    public Spu querySpuById(Long id) {
        //查询spu
        Spu spu = spuMapper.selectByPrimaryKey(id);
        if(spu==null){
            throw new LyException(ExceptionEnum.GOODS_NOT_FOND);
        }
        //查询sku
        spu.setSkus(querySkuByListId(id));
        //查询Detail
        spu.setSpuDetail(queyrDetailById(id));
        return spu;
    }

    public List<Sku> querySkuByIds(List<Long> ids) {
        List<Sku> skuList = skuMapper.selectByIdList(ids);
        if(CollectionUtils.isEmpty(skuList)){
            throw new LyException(ExceptionEnum.GOODS_SUK_NOT_FOND);
        }
        //查询库存
        querySkuToStock(skuList, ids);
        return skuList;
    }

    public Sku querySkuById(Long id) {
        Sku sku = this.skuMapper.selectByPrimaryKey(id);
        if(sku==null){
            throw new LyException(ExceptionEnum.GOODS_SUK_NOT_FOND);
        }
        return sku;
    }

    public void decreaseStock(List<CartDTO> carts) {
        for (CartDTO cart : carts) {
            int count = stockMapper.decreaseStock(cart.getSkuId(), cart.getNum());
            if (count != 1) {
                throw new LyException(ExceptionEnum.STOCK_NOT_ENOUGH);
            }
        }
    }
}
