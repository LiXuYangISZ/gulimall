package com.atguigu.gulimall.ware.service.impl;

import com.atguigu.common.utils.R;
import com.atguigu.gulimall.ware.feign.ProductFeignService;
import com.atguigu.common.to.SkuHasStockTo;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.atguigu.common.utils.PageUtils;
import com.atguigu.common.utils.Query;

import com.atguigu.gulimall.ware.dao.WareSkuDao;
import com.atguigu.gulimall.ware.entity.WareSkuEntity;
import com.atguigu.gulimall.ware.service.WareSkuService;


@Service("wareSkuService")
public class WareSkuServiceImpl extends ServiceImpl <WareSkuDao, WareSkuEntity> implements WareSkuService {

    @Autowired
    ProductFeignService productFeignService;

    @Override
    public PageUtils queryPage(Map <String, Object> params) {
        LambdaQueryWrapper <WareSkuEntity> queryWrapper = new LambdaQueryWrapper <>();
        String skuId = (String) params.get("skuId");
        String wareId = (String) params.get("wareId");
        queryWrapper.eq(StringUtils.isNotBlank(skuId), WareSkuEntity::getSkuId, skuId)
                .eq(StringUtils.isNotBlank(wareId), WareSkuEntity::getWareId, wareId);
        IPage <WareSkuEntity> page = this.page(
                new Query <WareSkuEntity>().getPage(params),
                queryWrapper
        );

        return new PageUtils(page);
    }

    @Override
    public void addStock(Long skuId, Long wareId, Integer skuNum) {
        LambdaQueryWrapper <WareSkuEntity> queryWrapper = new LambdaQueryWrapper <>();
        queryWrapper.eq(WareSkuEntity::getSkuId, skuId).eq(WareSkuEntity::getWareId, wareId);
        Integer count = baseMapper.selectCount(queryWrapper);
        if (count > 0) {
            baseMapper.addStock(skuId, wareId, skuNum);
        } else {
            WareSkuEntity wareSkuEntity = new WareSkuEntity();
            wareSkuEntity.setSkuId(skuId);
            wareSkuEntity.setWareId(wareId);
            wareSkuEntity.setStock(skuNum);
            wareSkuEntity.setStockLocked(0);
            // TODO 远程查询Sku的名字，如果失败，整个事务无需回滚
            // 方法一：try...catch...
            try {
                R r = productFeignService.getSkuInfo(skuId);
                Map<String,Object> data  = (Map <String, Object>) r.get("skuInfo");
                if(r.getCode() == 0 ){
                    wareSkuEntity.setSkuName((String) data.get("skuName"));
                }
            }catch (Exception e){

            }
            // 方法二：待补充（高级篇讲解）
            baseMapper.insert(wareSkuEntity);
        }

    }

    @Override
    public List <SkuHasStockTo> getSkusHasStock(List <Long> skuIds) {
        // 方法一：批量查询符合库存条件的sku的id（批量相对于）
        // Set<Long> skuIdSet = this.baseMapper.filterSkuIds(skuIds);
        // List <SkuHasStockVo> hasStockVos = skuIds.stream().map(skuId -> {
        //     SkuHasStockVo skuHasStockVo = new SkuHasStockVo();
        //     skuHasStockVo.setSkuId(skuId);
        //     skuHasStockVo.setHasStock(skuIdSet.contains(skuId));
        //     return skuHasStockVo;
        // }).collect(Collectors.toList());

        // 方法二：每次查找一个SKU的库存情况
        List <SkuHasStockTo> hasStockVos = skuIds.stream().map(skuId -> {
            Long count = this.baseMapper.getSkuStock(skuId);
            SkuHasStockTo skuHasStockTo = new SkuHasStockTo();
            skuHasStockTo.setSkuId(skuId);
            skuHasStockTo.setHasStock(count == null ? false : count > 0);
            return skuHasStockTo;
        }).collect(Collectors.toList());
        return hasStockVos;
    }

}