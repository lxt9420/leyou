package com.leyou.web;

import com.leyou.common.vo.PageResult;
import com.leyou.pojo.Goods;
import com.leyou.pojo.SearchRequest;
import com.leyou.service.SearchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

/**
 * author:lu
 * create time: 2020/2/15.
 */
@RestController
public class SearchController {
    @Autowired
    private SearchService searchService;
    /**
     * 搜索功能
     *
     */
    @PostMapping("page")
    public ResponseEntity<PageResult<Goods>> search(@RequestBody SearchRequest request){
        return ResponseEntity.ok(searchService.search(request));
    }
}
