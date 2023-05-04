package com.atguigu.gulimall.product.vo.front;

import com.atguigu.common.valid.AddGroup;
import com.atguigu.common.valid.UpdateGroup;
import com.atguigu.gulimall.product.entity.BrandEntity;
import com.atguigu.gulimall.product.entity.SkuImagesEntity;
import com.atguigu.gulimall.product.entity.SkuInfoEntity;
import com.atguigu.gulimall.product.entity.SpuInfoDescEntity;
import com.atguigu.gulimall.product.vo.back.BrandVo;
import lombok.Data;
import lombok.ToString;

import javax.validation.constraints.NotBlank;
import java.util.List;
import java.util.Set;

/**
 * @author lxy
 * @version 1.0
 * @Description 商品详细信息
 * @date 2023/5/4 10:27
 */
@Data
public class SkuItemVo {
    /**
     * SKU基本信息
     */
    SkuInfoEntity skuInfo;
    /**
     * 商品品牌
     */
    BrandEntity brand;
    /**
     * 库存
     */
    Boolean hasStock = true;
    /**
     * SKU图片信息
     */
    List<SkuImagesEntity> skuImages;
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
    @ToString
    public static class SkuItemSaleAttrVo{
        /**
         * 属性id
         */
        private Long attrId;
        /**
         * 属性名称
         */
        private String attrName;
        /**
         * 属性值和SkuIds 列表
         */
        private List<AttrValueWithSkuIdVo> attrValues;
    }

    /**
     * 属性值和SkuIdsVo
     */
    @Data
    public static class AttrValueWithSkuIdVo {
        private String attrValue;
        private String skuIds;
    }


    /**
     * SPU规格组
     */
    @Data
    @ToString
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
    @ToString
    public static class SpuBaseAttrVo{
        /**
         * 属性名称
         */
        private String attrName;
        /**
         * 属性值
         */
        private String attrValue;
        /**
         * 快速展示【是否展示在介绍上；0-否 1-是】
         */
        private Integer quickShow;
    }
}
