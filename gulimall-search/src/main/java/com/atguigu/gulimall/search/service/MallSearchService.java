package com.atguigu.gulimall.search.service;

import com.atguigu.gulimall.search.vo.SearchParam;

/**
 * @author lxy
 * @version 1.0
 * @Description
 * @date 2023/4/30 11:06
 */
public interface MallSearchService {
    /**
     *
     * @param param 检索所有参数
     * @return 返回检索的结果
     */
    Object search(SearchParam param);
}
