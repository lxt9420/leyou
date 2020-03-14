package com.leyou.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.leyou.client.BrandsClient;
import com.leyou.client.CategoryClient;
import com.leyou.client.GoodsClient;
import com.leyou.client.SpecificationClient;
import com.leyou.common.enums.ExceptionEnum;
import com.leyou.common.exception.LyException;
import com.leyou.common.utils.JsonUtils;
import com.leyou.common.vo.PageResult;
import com.leyou.item.pojo.*;
import com.leyou.pojo.Goods;
import com.leyou.pojo.SearchRequest;
import com.leyou.pojo.SearchResult;
import com.leyou.repository.GoodsRepository;
import org.apache.commons.lang3.math.NumberUtils;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.Operator;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.aggregations.Aggregation;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.Aggregations;
import org.elasticsearch.search.aggregations.bucket.terms.LongTerms;
import org.elasticsearch.search.aggregations.bucket.terms.StringTerms;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.elasticsearch.core.ElasticsearchTemplate;
import org.springframework.data.elasticsearch.core.aggregation.AggregatedPage;
import org.springframework.data.elasticsearch.core.query.FetchSourceFilter;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.*;
import java.util.stream.Collectors;

/**
 * author:lu
 * create time: 2020/2/14.
 */
@Service
public class SearchService {
     @Autowired
     private CategoryClient categoryClient;
     @Autowired
     private BrandsClient brandsClient;
     @Autowired
     private GoodsClient goodsClient;
     @Autowired
     private SpecificationClient specificationClient;
     @Autowired
     private GoodsRepository repository;
     @Autowired
     private ElasticsearchTemplate template;

    public Goods buildGoods(Spu spu){
        Long spuId=spu.getId();
        //查询分类
        List<Category> categories = categoryClient.
                queryCategoryByIds(Arrays.asList(spu.getCid1(), spu.getCid2(), spu.getCid3()));
        if(CollectionUtils.isEmpty(categories)){
         throw new LyException(ExceptionEnum.CATEGORY_NOT_FOND);
        }
        List<String> names = categories.stream().map(Category::getName).collect(Collectors.toList());
        Brand brand = brandsClient.queryBrandByid(spu.getBrandId());
        if(brand==null){
            throw new LyException(ExceptionEnum.BRAND_NOT_FOND);
        }
        //搜索字段
        String all=spu.getTitle()+ StringUtils.join(names," ") +brand.getName();
        //查询sku
        List<Sku> skusList = goodsClient.querySkuByListId(spuId);
        if(CollectionUtils.isEmpty(skusList)){
            throw new LyException(ExceptionEnum.GOODS_SUK_NOT_FOND);
        }
        //对sku处理
        List<Map<String,Object>> skus=new ArrayList<>();
        //价格集合
       Set<Long> priceList=new HashSet<>();
        for (Sku sku : skusList) {
            Map<String,Object> map=new HashMap<>();
            map.put("id",sku.getId());
            map.put("title",sku.getTitle());
            map.put("price",sku.getPrice());
            map.put("image",StringUtils.substringBefore(sku.getImages(),","));
            skus.add(map);
            //处理价格
            priceList.add(sku.getPrice());
        }
        //查询规格参数
        List<SpecParam> params = specificationClient.queryParamByGid(null, spu.getCid3(), true);
        if(CollectionUtils.isEmpty(params)){
            throw new LyException(ExceptionEnum.SPECGROUP_NO_FOND);
        }
        //查询商品详情
        SpuDetail spuDetail = goodsClient.queyrDetailById(spuId);
        //获取通用规格参数
        Map<Long, String> genericSpec = JsonUtils.parseMap(spuDetail.getGenericSpec(), Long.class, String.class);
        //获取特殊规格参数
        Map<Long, List<String>> specialSpec = JsonUtils.
                nativeRead(spuDetail.getSpecialSpec(), new TypeReference<Map<Long, List<String>>>(){});
        //规格参数，key是规格参数名字，值是规格参数值

        //规格参数
        HashMap<String, Object> specs = new HashMap<>();
        for (SpecParam param : params) {
            String key=param.getName();
            Object value="";
            //判断是否是通用规格
            if(param.getGeneric()){
                value=genericSpec.get(param.getId());
                //判断数字类型
                if(param.getNumeric()){
                     value=chooseSegment(value.toString(),param);
                }
            }else{
                value = specialSpec.get(param.getId());
            }
            specs.put(key,value);
        }
        Goods goods=new Goods();
        goods.setId(spu.getId());
        goods.setSubTitle(spu.getSubTitle());
        goods.setBrandId(spu.getBrandId());
        goods.setCid1(spu.getCid1());
        goods.setCid2(spu.getCid2());
        goods.setCid3(spu.getCid3());
        goods.setCreateTime(spu.getCreateTime());
        goods.setAll(all);
        goods.setPrice(priceList);
        goods.setSkus(JsonUtils.serialize(skus));
        goods.setSpecs(specs);
        return goods;
    }

    private String chooseSegment(String value, SpecParam p) {
        double val = NumberUtils.toDouble(value);
        String result = "其它";
        // 保存数值段
        for (String segment : p.getSegments().split(",")) {
            String[] segs = segment.split("-");
            // 获取数值范围
            double begin = NumberUtils.toDouble(segs[0]);
            double end = Double.MAX_VALUE;
            if(segs.length == 2){
                end = NumberUtils.toDouble(segs[1]);
            }
            // 判断是否在范围内
            if(val >= begin && val < end){
                if(segs.length == 1){
                    result = segs[0] + p.getUnit() + "以上";
                }else if(begin == 0){
                    result = segs[1] + p.getUnit() + "以下";
                }else{
                    result = segment + p.getUnit();
                }
                break;
            }
        }
        return result;
    }

    public PageResult<Goods> search(SearchRequest request) {
        String key=request.getKey();
        if(StringUtils.isBlank(key)){
            return null;
        }
        int page =request.getPage()-1;
        int size=request.getSize();
        //创建查询构建器
        NativeSearchQueryBuilder queryBuilder = new NativeSearchQueryBuilder();
        //0.结果过滤
        queryBuilder.withSourceFilter(new FetchSourceFilter(new String[]{"id","subTitle","skus"},null));
        //1.分页
        queryBuilder.withPageable(PageRequest.of(page,size));
        //2.搜索条件
      // QueryBuilder builderQuery = QueryBuilders.matchQuery("all", key);
        QueryBuilder builderQuery =buildBasicQuery(request);
        queryBuilder.withQuery(builderQuery);
        //3.查询聚合分类和品牌
        //3.1聚合分类
        String categoryAggName="category_agg";
        queryBuilder.addAggregation(AggregationBuilders.terms(categoryAggName).field("cid3"));
        //3.2聚合品牌
        String brandAggName="brand_agg";
        queryBuilder.addAggregation(AggregationBuilders.terms(brandAggName).field("brandId"));
        //4.查询
        AggregatedPage<Goods> result = template.queryForPage(queryBuilder.build(), Goods.class);
        //5解析结果
        //5.1分析分析结果
        long total = result.getTotalElements();
        int totalPages = result.getTotalPages();
        List<Goods> goodsList = result.getContent();
        //5.2解析聚合结果
        Aggregations aggs = result.getAggregations();
        List<Category> categorys=parseCategoryAgg(aggs.get(categoryAggName));
        List<Brand>  brands=parseBrandAgg(aggs.get(brandAggName));

        //完成规格参数聚合
        List<Map<String,Object>> spec=null;
        if(categorys !=null && categorys.size()==1){
            //商品分类存在，并且数量为1，可以聚合规格参数
             spec=buildSpecificationAgg(categorys.get(0).getId(),builderQuery);
        }
        return new SearchResult(total,totalPages,goodsList,categorys,brands,spec);
    }

   private QueryBuilder buildBasicQuery(SearchRequest request) {
        //创建布尔查询
        BoolQueryBuilder queryBuilder=QueryBuilders.boolQuery();
        //查询条件
        queryBuilder.must(QueryBuilders.matchQuery("all", request.getKey()));
        //过滤条件
        Map<String, Object> map = request.getFilter();
        for(Map.Entry<String,Object> entry :map.entrySet()){
            String key=entry.getKey();
           if( !"cid3".equals(key) && !"brandId".equals(key)){
                   key="specs."+key+".keyword";
            }
            queryBuilder.filter(QueryBuilders.termQuery(key,entry.getValue()));
        }
        return queryBuilder;
    }

    /**
     * 构建bool查询构建器，主要用于按照商品的一些参数条件来构建查询条件
     * @param
     * @return
     */
  /*  private QueryBuilder buildBasicQuery(SearchRequest request) {
        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();

        // 添加基本查询条件
        boolQueryBuilder.must(QueryBuilders.matchQuery("all", request.getKey()).operator(Operator.AND));

        // 添加过滤条件
        if (CollectionUtils.isEmpty(request.getFilter())){
            return boolQueryBuilder;
        }

        //获取到过滤条件进行遍历
        for (Map.Entry<String, Object> entry : request.getFilter().entrySet()) {

            String key = entry.getKey();
            // 如果过滤条件是“品牌”, 过滤的字段名：brandId
            if (StringUtils.equals("品牌", key)) {
                key = "brandId";
            } else if (StringUtils.equals("分类", key)) {
                // 如果是“分类”，过滤字段名：cid3
                key = "cid3";
            } else {
                // 如果是规格参数名，过滤字段名：specs.key.keyword
                key = "specs." + key + ".keyword";
            }
            boolQueryBuilder.filter(QueryBuilders.termQuery(key, entry.getValue()));
        }

        return boolQueryBuilder;
    }
*/

    private List<Map<String,Object>> buildSpecificationAgg(Long cid, QueryBuilder builderQuery) {
        List<Map<String,Object>> specs=new ArrayList<>();
        //1.查询需要聚合的规格参数
        List<SpecParam> params = specificationClient.queryParamByGid(null, cid, true);
        //2.声明聚合
        NativeSearchQueryBuilder queryBuilder = new NativeSearchQueryBuilder();
        //2.1带上条件查询
        queryBuilder.withQuery(builderQuery);
        //2.2聚合
        for (SpecParam param : params) {
            String name = param.getName();
            queryBuilder.addAggregation(AggregationBuilders.terms(name).field("specs."+name+".keyword"));
        }
        //3聚合结果
        AggregatedPage<Goods> result = template.queryForPage(queryBuilder.build(), Goods.class);
        //4分析结果
        Aggregations aggs = result.getAggregations();
       System.out.println(aggs);
        for (SpecParam param : params) {
            //规格参数名
            String name=param.getName();
            StringTerms terms=aggs.get(name);
            Map<String,Object> map=new HashMap<>();
            map.put("k",name);
            map.put("options",terms.getBuckets().stream().map(b ->b.getKeyAsString()).collect(Collectors.toList()));
            specs.add(map);
        }
        return specs;
    }



    private List<Brand> parseBrandAgg(LongTerms terms) {
        try{
            List<Long> ids = terms.getBuckets().stream().map(b -> b.getKeyAsNumber().longValue())
                    .collect(Collectors.toList());
            List<Brand> brands = brandsClient.queryBrandByIds(ids);
            return brands;
        }catch (Exception e){
            return null;
        }
    }
    private List<Category> parseCategoryAgg(LongTerms terms) {
        try{
            List<Long> ids = terms.getBuckets().stream().map(b -> b.getKeyAsNumber().longValue())
                                  .collect(Collectors.toList());
            List<Category> categories = categoryClient.queryCategoryByIds(ids);
            return categories;
        }catch (Exception e){
            return null;
        }
    }

    public void listenInsertOrUpdate(Long spuId) {
        Spu spu = goodsClient.querySpuById(spuId);
        Goods goods = buildGoods(spu);
        repository.save(goods);
    }
    public void deleteIndex(Long spuId){
        repository.deleteById(spuId);
    }
}
