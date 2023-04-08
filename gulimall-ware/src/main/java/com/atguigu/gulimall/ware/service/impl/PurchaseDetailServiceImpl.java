package com.atguigu.gulimall.ware.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Service;

import java.util.Map;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.atguigu.common.utils.PageUtils;
import com.atguigu.common.utils.Query;

import com.atguigu.gulimall.ware.dao.PurchaseDetailDao;
import com.atguigu.gulimall.ware.entity.PurchaseDetailEntity;
import com.atguigu.gulimall.ware.service.PurchaseDetailService;


@Service("purchaseDetailService")
public class PurchaseDetailServiceImpl extends ServiceImpl <PurchaseDetailDao, PurchaseDetailEntity> implements PurchaseDetailService {

    @Override
    public PageUtils queryPage(Map <String, Object> params) {
        IPage <PurchaseDetailEntity> page = this.page(
                new Query <PurchaseDetailEntity>().getPage(params),
                new QueryWrapper <PurchaseDetailEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public PageUtils queryPageByCondition(Map <String, Object> params) {
        String key = (String) params.get("key");
        String status = (String) params.get("status");
        String wareId = (String) params.get("wareId");
        LambdaQueryWrapper <PurchaseDetailEntity> queryWrapper = new LambdaQueryWrapper <>();
        queryWrapper.and(StringUtils.isNotBlank(key), wrapper -> wrapper
                .eq(PurchaseDetailEntity::getPurchaseId, key).or()
                .eq(PurchaseDetailEntity::getSkuId, key))
                .eq(StringUtils.isNotBlank(status), PurchaseDetailEntity::getStatus, status)
                .eq(StringUtils.isNotBlank(wareId), PurchaseDetailEntity::getWareId, wareId);

        IPage <PurchaseDetailEntity> page = this.page(
                new Query <PurchaseDetailEntity>().getPage(params),
                queryWrapper
        );

        return new PageUtils(page);
    }

}