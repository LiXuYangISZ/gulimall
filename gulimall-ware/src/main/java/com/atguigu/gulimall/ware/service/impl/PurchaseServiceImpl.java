package com.atguigu.gulimall.ware.service.impl;

import com.atguigu.common.constant.ware.PurchaseDetailStatusEnum;
import com.atguigu.common.constant.ware.PurchaseStatusEnum;
import com.atguigu.gulimall.ware.entity.PurchaseDetailEntity;
import com.atguigu.gulimall.ware.service.PurchaseDetailService;
import com.atguigu.gulimall.ware.vo.MergeVo;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.atguigu.common.utils.PageUtils;
import com.atguigu.common.utils.Query;

import com.atguigu.gulimall.ware.dao.PurchaseDao;
import com.atguigu.gulimall.ware.entity.PurchaseEntity;
import com.atguigu.gulimall.ware.service.PurchaseService;
import org.springframework.transaction.annotation.Transactional;


@Service("purchaseService")
public class PurchaseServiceImpl extends ServiceImpl <PurchaseDao, PurchaseEntity> implements PurchaseService {

    @Autowired
    PurchaseDetailService purchaseDetailService;

    @Override
    public PageUtils queryPage(Map <String, Object> params) {
        LambdaQueryWrapper <PurchaseEntity> queryWrapper = new LambdaQueryWrapper <>();
        String status = (String) params.get("status");
        String key = (String) params.get("key");
        queryWrapper.and(StringUtils.isNotBlank(key), wrapper -> wrapper
                .like(PurchaseEntity::getAssigneeName, key).or()
                .eq(PurchaseEntity::getWareId, key))
                .eq(StringUtils.isNotBlank(status), PurchaseEntity::getStatus, status);
        IPage <PurchaseEntity> page = this.page(
                new Query <PurchaseEntity>().getPage(params),
                queryWrapper
        );

        return new PageUtils(page);
    }

    @Override
    public List <PurchaseEntity> queryUnreceiveList() {
        List <PurchaseEntity> purchaseEntities = this.list(new LambdaQueryWrapper <PurchaseEntity>()
                .eq(PurchaseEntity::getStatus, 0).or()
                .eq(PurchaseEntity::getStatus, 1));
        return purchaseEntities;
    }

    @Transactional
    @Override
    public void mergePurchase(MergeVo mergeVo) {
        Long purchaseId = mergeVo.getPurchaseId();
        if(purchaseId == null){
            // 系统创建一个新的采购单
            PurchaseEntity purchaseEntity = new PurchaseEntity();
            purchaseEntity.setPriority(2);
            purchaseEntity.setStatus(PurchaseStatusEnum.CREATED.getCode());
            this.save(purchaseEntity);
            purchaseId = purchaseEntity.getId();
        }
        final Long finalPurchaseId = purchaseId;
        List <PurchaseDetailEntity> purchaseDetailEntities = mergeVo.getItems().stream().map(item -> {
            PurchaseDetailEntity purchaseDetailEntity = new PurchaseDetailEntity();
            purchaseDetailEntity.setId(item);
            purchaseDetailEntity.setStatus(PurchaseDetailStatusEnum.ASSIGNED.getCode());
            purchaseDetailEntity.setPurchaseId(finalPurchaseId);
            return purchaseDetailEntity;
        }).collect(Collectors.toList());
        // 更新采购详情
        purchaseDetailService.updateBatchById(purchaseDetailEntities);
    }

}