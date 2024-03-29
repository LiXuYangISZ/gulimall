package com.atguigu.gulimall.search.vo;

import lombok.Data;

import java.util.List;

/**
 * @author lxy
 * @version 1.0
 * @Description 封装页面所有可能传递过来的查询条件
 *  请求路径中的参数的情况：
 *  ①如果是每种品牌/属性只能有单个： catalog3Id=225&keyword=小米&sort=saleCount_asc&hasStock=0/1&brandId=1&attrs=1_5寸&pageNum=0
 *  ②如果是每种品牌/属性允许多选：  catalog3Id=225&keyword=小米&sort=saleCount_asc&hasStock=0/1&brandId=1&attrs=1_5寸:8寸&attrs=2_16G:8G&pageNum=0
 * @date 2023/4/30 11:08
 */
@Data
public class SearchParam {
    /**
     * 页面传递归来的全文匹配关键字
     */
    private String keyword;
    /**
     * 三级分类id
     */
    private Long catalog3Id;
    /**
     * 排序条件【这里也可以设置成枚举】
     * sort=saleCount_asc/desc
     * sort=skuPrice_asc/desc
     * sort=hotScore_asc/desc
     */
    private String sort;
    /**
     * 是否显示有货【0-无库存，1-有库存】,默认都是1
     */
    private Integer hasStock;
    /**
     * 价格区间查询
     */
    private String skuPrice;
    // /**
    //  * 品牌id【可以多选】
    //  */
    // private List<Long> brandId;
    /**
     * 品牌id【只支持单选。之后如果开启品牌的多选，可以brandId=1:16:15这样设计】
     */
    private Long brandId;
    /**
     * 属性
     */
    private List<String> attrs;
    /**
     * 页码
     */
    private Integer pageNum = 1;
    /**
     * 查询字符串
     */
    private String queryString;

}
