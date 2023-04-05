package com.atguigu.gulimall.product.service.impl;

import com.atguigu.common.utils.PageUtils;
import com.atguigu.common.utils.Query;
import com.atguigu.gulimall.product.entity.*;
import com.atguigu.gulimall.product.service.*;
import com.atguigu.gulimall.product.vo.product.BaseAttrs;
import com.atguigu.gulimall.product.vo.product.Images;
import com.atguigu.gulimall.product.vo.product.Skus;
import com.atguigu.gulimall.product.vo.product.SpuSaveVo;
import org.bouncycastle.util.Arrays;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

import com.atguigu.gulimall.product.dao.SpuInfoDao;

import javax.imageio.ImageIO;


@Service("spuInfoService")
public class SpuInfoServiceImpl extends ServiceImpl<SpuInfoDao, SpuInfoEntity> implements SpuInfoService {

    @Autowired
    SpuInfoDescService spuInfoDescService;

    @Autowired
    SpuImagesService spuImagesService;

    @Autowired
    ProductAttrValueService productAttrValueService;

    @Autowired
    AttrService attrService;

    @Autowired
    SkuInfoService skuInfoService;

    @Autowired
    SkuImagesService skuImagesService;

    @Autowired
    SkuSaleAttrValueService skuSaleAttrValueService;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<SpuInfoEntity> page = this.page(
                new Query <SpuInfoEntity>().getPage(params),
                new QueryWrapper<SpuInfoEntity>()
        );

        return new PageUtils(page);
    }

    /**
     * 保存商品信息
     * @param vo
     */
    @Override
    public void saveSpuInfo(SpuSaveVo vo) {
        // 1.保存Spu基本信息 pms_spu_info
        SpuInfoEntity spuInfoEntity = new SpuInfoEntity();
        BeanUtils.copyProperties(vo,spuInfoEntity);
        this.save(spuInfoEntity);

        // 2.保存Spu的描述图片 pms_spu_info_desc
        SpuInfoDescEntity spuInfoDescEntity = new SpuInfoDescEntity();
        spuInfoDescEntity.setSpuId(spuInfoEntity.getId());
        List <String> decript = vo.getDecript();
        if(decript!=null && decript.size() >0){
            spuInfoDescEntity.setDecript(String.join(",",decript));
        }
        spuInfoDescService.save(spuInfoDescEntity);

        // 3.保存Spu的图片集 pms_spu_images
        List <String> spuImages = vo.getImages();
        if(spuImages != null && spuImages.size() > 0){
            List <SpuImagesEntity> spuImagesEntities = spuImages.stream().map(image -> {
                SpuImagesEntity spuImagesEntity = new SpuImagesEntity();
                spuImagesEntity.setSpuId(spuInfoEntity.getId());
                spuImagesEntity.setImgUrl(image);
                spuImagesEntity.setImgName(image.substring(image.lastIndexOf("/")));
                return spuImagesEntity;
            }).collect(Collectors.toList());
            spuImagesService.saveBatch(spuImagesEntities);
        }

        // 4.保存Spu的规格参数 pms_product_attr_value
        List <BaseAttrs> baseAttrs = vo.getBaseAttrs();
        if(baseAttrs!=null && baseAttrs.size() > 0){
            List <ProductAttrValueEntity> productAttrValueEntities = baseAttrs.stream().map(baseAttr -> {
                ProductAttrValueEntity productAttrValueEntity = new ProductAttrValueEntity();
                productAttrValueEntity.setSpuId(spuInfoEntity.getId());
                productAttrValueEntity.setAttrId(baseAttr.getAttrId());
                productAttrValueEntity.setAttrValue(baseAttr.getAttrValues());
                productAttrValueEntity.setQuickShow(baseAttr.getShowDesc());
                AttrEntity attr = attrService.getById(baseAttr.getAttrId());
                productAttrValueEntity.setAttrName(attr.getAttrName());
                return productAttrValueEntity;
            }).collect(Collectors.toList());
            productAttrValueService.saveBatch(productAttrValueEntities);
        }

        // 5.保存Spu的积分信息 gulimall_sms ==> sms_spu_bounds


        // 6.保存当前Spu所有的Sku信息
        List <Skus> skus = vo.getSkus();
        skus.forEach(sku->{
            // 寻找默认图片
            String defaultImg = "";
            for (Images image : sku.getImages()) {
                if(image.getDefaultImg().equals(1)){
                    defaultImg = image.getImgUrl();
                }
            }
            // 6.1）Sku的基本信息 pms_sku_info
            SkuInfoEntity skuInfoEntity = new SkuInfoEntity();
            BeanUtils.copyProperties(sku,skuInfoEntity);
            skuInfoEntity.setSpuId(spuInfoEntity.getId());
            skuInfoEntity.setCatalogId(vo.getCatalogId());
            skuInfoEntity.setBrandId(vo.getBrandId());
            skuInfoEntity.setSkuDefaultImg(defaultImg);
            skuInfoEntity.setSaleCount(0L);
            skuInfoService.save(skuInfoEntity);
            Long skuId = skuInfoEntity.getSkuId();

            // 6.2) sku的图片纤细 pms_sku_images
            List <SkuImagesEntity> skuImagesEntities = sku.getImages().stream().map(image -> {
                SkuImagesEntity skuImagesEntity = new SkuImagesEntity();
                skuImagesEntity.setSkuId(skuId);
                skuImagesEntity.setImgUrl(image.getImgUrl());
                skuImagesEntity.setDefaultImg(image.getDefaultImg());
                return skuImagesEntity;
            }).collect(Collectors.toList());
            skuImagesService.saveBatch(skuImagesEntities);

            // 6.3) sku的销售属性 pms_sku_sale_attr_value
            List <SkuSaleAttrValueEntity> skuSaleAttrValueEntities = sku.getAttr().stream().map(attr -> {
                SkuSaleAttrValueEntity skuSaleAttrValueEntity = new SkuSaleAttrValueEntity();
                BeanUtils.copyProperties(attr, skuSaleAttrValueEntity);
                skuSaleAttrValueEntity.setSkuId(skuId);
                return skuSaleAttrValueEntity;
            }).collect(Collectors.toList());
            skuSaleAttrValueService.saveBatch(skuSaleAttrValueEntities);


        });


        // 6.4) sku的优惠、满减等信息 gulimall_sms ==> sms_sku_full_reduction/sms_sku_ladder/sms_member_price

    }

}