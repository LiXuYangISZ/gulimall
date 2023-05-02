package com.atguigu.gulimall.search.vo;

import com.atguigu.common.to.SkuEsModel;
import lombok.Data;

import java.util.List;

/**
 * @author lxy
 * @version 1.0
 * @Description 返回页面的检索结果
 * @date 2023/4/30 11:44
 */
@Data
public class SearchResult {
    /**
     * 查询到的所有商品信息
     */
    private List<SkuEsModel> products;
    /**
     * 当前页码
     */
    private Integer pageNum;
    /**
     *  总记录数
     */
    private Long total;
    /**
     * 总页码
     */
    private Long totalPages;
    /**
     * 当前查询的结果，所有涉及到的品牌
     */
    private List<BrandVo> brands;
    /**
     * 当前查询的结果，所有涉及到的属性
     */
    private List<AttrVo> attrs;
    /**
     * 当前查询的结果，所有涉及到的分类
     */
    private List<CatalogVo> catalogs;

    /**
     * 品牌VO
     */
    @Data
    public static class BrandVo{
        /**
         * 品牌id
         */
        private Long brandId;
        /**
         * 品牌名称
         */
        private String brandName;
        /**
         * 品牌图片
         */
        private String brandImg;
    }

    /**
     * 属性VO
     */
    @Data
    public static class AttrVo{
        private Long attrId;
        private String attrName;
        private List<String> attrValue;
    }

    /**
     * 分类VO
     */
    @Data
    public static class CatalogVo{
        private Long catalogId;
        private String catalogName;
    }
}
