package com.atguigu.gulimall.search.service;

import com.atguigu.gulimall.search.vo.SearchParam;
import com.atguigu.gulimall.search.vo.SearchResult;

/**
 * @author lxy
 * @version 1.0
 * @Description
 * @date 2023/4/30 11:06
 */
public interface MallSearchService {
    /**
     *
     * @param param 根据参数检索所有商品
     * @return 返回检索的结果，里面包含页面需要的所有信息
     */
    SearchResult search(SearchParam param);
}
