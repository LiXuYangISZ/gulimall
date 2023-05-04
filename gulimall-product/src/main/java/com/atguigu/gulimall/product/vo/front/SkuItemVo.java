package com.atguigu.gulimall.product.vo.front;

import com.atguigu.gulimall.product.entity.SkuImagesEntity;
import com.atguigu.gulimall.product.entity.SkuInfoEntity;
import com.atguigu.gulimall.product.entity.SpuInfoDescEntity;
import lombok.Data;

import java.util.List;

/**
 * @author lxy
 * @version 1.0
 * @Description 商品详细信息
 * @date 2023/5/4 10:27
 */
public class SkuItemVo {
    /**
     * SKU基本信息
     */
    SkuInfoEntity skuInfo;
    /**
     * SKU图片信息
     */
    SkuImagesEntity skuImages;
    /**
     * SPU销售属性
     */
    List<SkuItemSaleAttrVo> saleAttrs;
    /**
     * SKU图片信息
     */
    SpuInfoDescEntity spuInfoDesc;
    /**
     * SPU的规格参数信息
     */
    List<SpuItemAttrGroupVo> groupAttrs;
    /**
     * 销售属性
     */
    @Data
    public static class SkuItemSaleAttrVo{
        /**
         * 属性id
         */
        private Long attrId;
        /**
         * 属性名称
         */
        private String attrName;
        private List <String> attrValues;
    }

    /**
     * SPU规格组
     */
    @Data
    public static class SpuItemAttrGroupVo{
        /**
         * 属性组名称
         */
        private String groupName;
        /**
         * 属性列表
         */
        private List<SpuBaseAttrVo> groupValue;
    }

    /**
     * 基本规格属性
     */
    @Data
    public static class SpuBaseAttrVo{
        /**
         * 属性名称
         */
        private String attrName;
        /**
         * 属性值
         */
        private String attrValue;
    }
}
