package com.atguigu.gulimall.coupon.service.impl;

import com.atguigu.common.to.MemberPrice;
import com.atguigu.common.to.SkuReductionTo;
import com.atguigu.gulimall.coupon.entity.MemberPriceEntity;
import com.atguigu.gulimall.coupon.entity.SkuLadderEntity;
import com.atguigu.gulimall.coupon.service.MemberPriceService;
import com.atguigu.gulimall.coupon.service.SkuLadderService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.atguigu.common.utils.PageUtils;
import com.atguigu.common.utils.Query;

import com.atguigu.gulimall.coupon.dao.SkuFullReductionDao;
import com.atguigu.gulimall.coupon.entity.SkuFullReductionEntity;
import com.atguigu.gulimall.coupon.service.SkuFullReductionService;
import org.springframework.transaction.annotation.Transactional;


@Service("skuFullReductionService")
public class SkuFullReductionServiceImpl extends ServiceImpl <SkuFullReductionDao, SkuFullReductionEntity> implements SkuFullReductionService {

    @Autowired
    SkuLadderService skuLadderService;

    @Autowired
    MemberPriceService memberPriceService;

    @Override
    public PageUtils queryPage(Map <String, Object> params) {
        IPage <SkuFullReductionEntity> page = this.page(
                new Query <SkuFullReductionEntity>().getPage(params),
                new QueryWrapper <SkuFullReductionEntity>()
        );

        return new PageUtils(page);
    }

    @Transactional
    @Override
    public void saveSkuReduction(SkuReductionTo skuReductionTo) {
        // 1、保存满减信息
        if (skuReductionTo.getFullPrice().compareTo(BigDecimal.ZERO) > 0 && skuReductionTo.getReducePrice().compareTo(BigDecimal.ZERO) > 0) {
            SkuFullReductionEntity skuFullReductionEntity = new SkuFullReductionEntity();
            BeanUtils.copyProperties(skuReductionTo, skuFullReductionEntity);
            skuFullReductionEntity.setAddOther(skuReductionTo.getPriceStatus());
            this.save(skuFullReductionEntity);
        }
        // 2、保存折扣信息
        if (skuReductionTo.getFullCount().compareTo(0) > 0 && skuReductionTo.getDiscount().compareTo(BigDecimal.ZERO) > 0) {
            SkuLadderEntity skuLadderEntity = new SkuLadderEntity();
            BeanUtils.copyProperties(skuReductionTo, skuLadderEntity);
            skuLadderEntity.setAddOther(skuReductionTo.getCountStatus());
            skuLadderEntity.setPrice(skuReductionTo.getPrice().multiply(skuReductionTo.getDiscount()));
            skuLadderService.save(skuLadderEntity);
        }
        // 3.保存会员价格信息
        List <MemberPrice> memberPrices = skuReductionTo.getMemberPrice();
        List <MemberPriceEntity> memberPriceEntities = memberPrices.stream()
                .filter(memberPrice -> memberPrice.getPrice().compareTo(BigDecimal.ZERO) > 0)
                .map(memberPrice -> {
                    MemberPriceEntity memberPriceEntity = new MemberPriceEntity();
                    memberPriceEntity.setSkuId(skuReductionTo.getSkuId());
                    memberPriceEntity.setMemberPrice(memberPrice.getPrice());
                    memberPriceEntity.setMemberLevelId(memberPrice.getId());
                    memberPriceEntity.setMemberLevelName(memberPrice.getName());
                    memberPriceEntity.setAddOther(1);
                    return memberPriceEntity;
                }).collect(Collectors.toList());
        memberPriceService.saveBatch(memberPriceEntities);
    }

}