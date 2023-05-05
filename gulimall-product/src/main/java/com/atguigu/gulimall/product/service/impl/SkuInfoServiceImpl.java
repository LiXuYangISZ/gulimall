package com.atguigu.gulimall.product.service.impl;

import com.atguigu.common.utils.PageUtils;
import com.atguigu.common.utils.Query;
import com.atguigu.gulimall.product.entity.*;
import com.atguigu.gulimall.product.service.*;
import com.atguigu.gulimall.product.vo.front.SkuItemVo;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ThreadPoolExecutor;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

import com.atguigu.gulimall.product.dao.SkuInfoDao;


@Service("skuInfoService")
public class SkuInfoServiceImpl extends ServiceImpl <SkuInfoDao, SkuInfoEntity> implements SkuInfoService {

    @Autowired
    SkuImagesService skuImagesService;

    @Autowired
    SpuInfoDescService spuInfoDescService;

    @Autowired
    AttrGroupService attrGroupService;

    @Autowired
    SkuSaleAttrValueService skuSaleAttrValueService;

    @Autowired
    BrandService brandService;

    @Autowired
    ThreadPoolExecutor threadPoolExecutor;

    @Override
    public PageUtils queryPage(Map <String, Object> params) {
        IPage <SkuInfoEntity> page = this.page(
                new Query <SkuInfoEntity>().getPage(params),
                new QueryWrapper <>()
        );

        return new PageUtils(page);
    }

    @Override
    public PageUtils queryPageByCondition(Map <String, Object> params) {
        String key = (String) params.get("key");
        String catelogId = (String) params.get("catelogId");
        String brandId = (String) params.get("brandId");
        String min = (String) params.get("min");
        String max = (String) params.get("max");

        LambdaQueryWrapper <SkuInfoEntity> queryWrapper = new LambdaQueryWrapper <>();

        queryWrapper.and(StringUtils.isNotBlank(key),
                wrapper -> wrapper.eq(SkuInfoEntity::getSkuId, key).or().like(SkuInfoEntity::getSkuName, key)
        ).eq(StringUtils.isNotBlank(catelogId) && !"0".equals(catelogId), SkuInfoEntity::getCatalogId, catelogId
        ).eq(StringUtils.isNotBlank(brandId) && !"0".equals(brandId), SkuInfoEntity::getBrandId, brandId
        ).le(StringUtils.isNotBlank(max) && new BigDecimal(max).compareTo(BigDecimal.ZERO) > 0, SkuInfoEntity::getPrice, max
        ).ge(StringUtils.isNotBlank(min), SkuInfoEntity::getPrice, min);

        IPage <SkuInfoEntity> page = this.page(
                new Query <SkuInfoEntity>().getPage(params),
                queryWrapper
        );

        return new PageUtils(page);
    }

    @Override
    public List <SkuInfoEntity> getSkusBySpuId(Long spuId) {
        List <SkuInfoEntity> list = this.list(new LambdaQueryWrapper <SkuInfoEntity>().eq(SkuInfoEntity::getSpuId, spuId));
        return list;
    }

    /**
     * 为商品详情添加缓存
     * TODO 后期后台增加商品删除和修改功能了,可以在那些方法上加 @CacheEvict 来移除缓存~
     * @param skuId
     * @return
     */
    @Cacheable(value = "item",key = "'itemInfo'+#root.args[0]")
    @Override
    public SkuItemVo item(Long skuId){
        SkuItemVo skuItemVo = new SkuItemVo();

        CompletableFuture <SkuInfoEntity> infoFuture = CompletableFuture.supplyAsync(() -> {
            // 1、获取SKU基本信息 pms_sku_info
            SkuInfoEntity skuInfo = this.getById(skuId);
            skuItemVo.setSkuInfo(skuInfo);
            return skuInfo;
        }, threadPoolExecutor);

        CompletableFuture <Void> saleAttrFuture = infoFuture.thenAcceptAsync((res) -> {
            // 3、获取SPU销售属性组合
            List <SkuItemVo.SkuItemSaleAttrVo> saleAttrs = skuSaleAttrValueService.getSaleAttrsBySpuId(res.getSpuId());
            skuItemVo.setSaleAttrs(saleAttrs);
        }, threadPoolExecutor);

        CompletableFuture <Void> descFuture = infoFuture.thenAcceptAsync((res) -> {
            // 4、获取SPU的介绍(下方一堆介绍图)
            SpuInfoDescEntity spuInfo = spuInfoDescService.getById(res.getSpuId());
            skuItemVo.setSpuInfoDesc(spuInfo);
        }, threadPoolExecutor);

        CompletableFuture <Void> baseAttrFuture = infoFuture.thenAcceptAsync((res) -> {
            // 5、获取SPU的规格参数信息
            List <SkuItemVo.SpuItemAttrGroupVo> attrGroups = attrGroupService.getAttrGroupWithAttrsBySpuId(res.getSpuId(), res.getCatalogId());
            skuItemVo.setGroupAttrs(attrGroups);
        }, threadPoolExecutor);

        CompletableFuture <Void> brandFuture = infoFuture.thenAcceptAsync((res) -> {
            // 6、商品品牌
            BrandEntity brand = brandService.getById(res.getBrandId());
            skuItemVo.setBrand(brand);
        }, threadPoolExecutor);

        // 获取SKU图片信息的,无需使用infoFuture的结果,所有可以另开一个异步任务!
        CompletableFuture <Void> imageFuture = CompletableFuture.runAsync(() -> {
            // 2、获取SKU图片信息 pms_sku_images
            List <SkuImagesEntity> skuImages = skuImagesService.getImagesBySkuId(skuId);
            skuItemVo.setSkuImages(skuImages);
        }, threadPoolExecutor);

        // 等待所有任务都完成[由于infoFuture被其他任务所依赖着,所有其他任务完成他肯定也完成了~]
        try {
            CompletableFuture.allOf(saleAttrFuture,descFuture,baseAttrFuture,brandFuture,imageFuture).get();
        } catch (Exception e) {
            log.error("查询商品详情失败~,原因:{}",e);
        }

        return skuItemVo;
    }

}