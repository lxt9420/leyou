package com.leyou.repository;

import com.leyou.pojo.Goods;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

/**
 * author:lu
 * create time: 2020/2/14.
 */
public interface GoodsRepository extends ElasticsearchRepository<Goods,Long> {
}
