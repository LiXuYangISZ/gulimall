package com.atguigu.gulimall.product.service.impl;

import com.atguigu.common.utils.PageUtils;
import com.atguigu.common.utils.Query;
import com.atguigu.gulimall.product.vo.front.SkuItemVo;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

import com.atguigu.gulimall.product.dao.SkuInfoDao;
import com.atguigu.gulimall.product.entity.SkuInfoEntity;
import com.atguigu.gulimall.product.service.SkuInfoService;


@Service("skuInfoService")
public class SkuInfoServiceImpl extends ServiceImpl <SkuInfoDao, SkuInfoEntity> implements SkuInfoService {

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

    @Override
    public SkuItemVo item(Long skuId) {
        // 1、获取SKU基本信息 pms_sku_info

        // 2、获取SKU图片信息 pms_sku_images

        // 3、获取SPU销售属性组合

        // 4、获取SPU的介绍

        // 5、获取SPU的规格参数信息
        return null;
    }

}