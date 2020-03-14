package com.leyou.repository;

import com.leyou.client.GoodsClient;
import com.leyou.common.vo.PageResult;
import com.leyou.item.pojo.Spu;
import com.leyou.pojo.Goods;
import com.leyou.service.SearchService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.elasticsearch.core.ElasticsearchTemplate;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.stream.Collectors;

import static org.junit.Assert.*;

/**
 * author:lu
 * create time: 2020/2/14.
 */
@RunWith(SpringRunner.class)
@SpringBootTest
public class GoodsRepositoryTest {

    @Autowired
    private GoodsRepository goodsRepository;
    @Autowired
    private ElasticsearchTemplate elasticsearchTemplate;
    @Autowired
    private GoodsClient goodsClient;
    @Autowired
    private SearchService searchService;

    @Test
    public void testCreateIndex(){
        elasticsearchTemplate.createIndex(Goods.class);
        elasticsearchTemplate.putMapping(Goods.class);
    }
    @Test
    public void loadData(){
        int page=1;
        int rows=100;
        int size=0;
        do{
            PageResult<Spu> spuPageResult = goodsClient.querySpuByPage(page, rows, true, null);
            List<Spu> items = spuPageResult.getItems();
            if(CollectionUtils.isEmpty(items)){
                break;
            }
            List<Goods> collect = items.stream().map(searchService::buildGoods).collect(Collectors.toList());
            //存入索引库
            goodsRepository.saveAll(collect);
            //翻页
            page++;
            size=items.size();

        }while (size==100);

    }
}