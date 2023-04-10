package com.atguigu.gulimall.ware.service.impl;

import com.atguigu.common.constant.ware.PurchaseDetailStatusEnum;
import com.atguigu.common.constant.ware.PurchaseStatusEnum;
import com.atguigu.gulimall.ware.entity.PurchaseDetailEntity;
import com.atguigu.gulimall.ware.service.PurchaseDetailService;
import com.atguigu.gulimall.ware.service.WareSkuService;
import com.atguigu.gulimall.ware.vo.MergeVo;
import com.atguigu.gulimall.ware.vo.PurchaseDoneVo;
import com.atguigu.gulimall.ware.vo.PurchaseItemDoneVo;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

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

    @Autowired
    WareSkuService wareSkuService;

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
        if (purchaseId == null) {
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

    @Override
    public void receive(List <Long> ids) {
        // TODO 这里忽略 采购员只能看到自己的采购单的这些细节
        // 1.确认当前采购单是新建或者已分配状态
        List <PurchaseEntity> purchaseEntities = ids.stream().filter(id -> {
            PurchaseEntity purchaseEntity = this.getById(id);
            return purchaseEntity.getStatus().equals(PurchaseStatusEnum.ASSIGNED.getCode())
                    || purchaseEntity.getStatus().equals(PurchaseStatusEnum.CREATED.getCode());
        }).map(id -> {
            PurchaseEntity purchaseEntity = new PurchaseEntity();
            purchaseEntity.setId(id);
            purchaseEntity.setStatus(PurchaseStatusEnum.RECEIVED.getCode());
            return purchaseEntity;
        }).collect(Collectors.toList());
        // 2.改变采购单的状态
        this.updateBatchById(purchaseEntities);
        // 3.改变采购项的状态
        purchaseEntities.forEach(item -> {
            LambdaUpdateWrapper <PurchaseDetailEntity> updateWrapper = new LambdaUpdateWrapper <>();
            updateWrapper.eq(PurchaseDetailEntity::getPurchaseId, item.getId());
            PurchaseDetailEntity purchaseDetailEntity = new PurchaseDetailEntity();
            purchaseDetailEntity.setStatus(PurchaseDetailStatusEnum.BUYING.getCode());
            purchaseDetailService.update(purchaseDetailEntity, updateWrapper);
        });
    }

    @Transactional
    @Override
    public void done(PurchaseDoneVo purchaseDoneVo) {
        // 1.修改采购项的信息
        List <PurchaseItemDoneVo> items = purchaseDoneVo.getItems();
        boolean flag = true;
        List <PurchaseDetailEntity> detailEntities = new ArrayList <>();
        BigDecimal price = BigDecimal.ZERO;
        for (PurchaseItemDoneVo item : items) {
            PurchaseDetailEntity purchaseDetailEntity = new PurchaseDetailEntity();
            purchaseDetailEntity.setStatus(item.getStatus());
            purchaseDetailEntity.setId(item.getItemId());
            if (PurchaseDetailStatusEnum.HASERROR.getCode().equals(item.getStatus())) {
                flag = false;
            } else {
                // 3.商品库存增加
                PurchaseDetailEntity detailEntity = purchaseDetailService.getById(item.getItemId());
                wareSkuService.addStock(detailEntity.getSkuId(), detailEntity.getWareId(), detailEntity.getSkuNum());
                // 采购价格计算
                price = price.add(detailEntity.getSkuPrice().multiply(BigDecimal.valueOf(detailEntity.getSkuNum())));
            }
            detailEntities.add(purchaseDetailEntity);
        }
        purchaseDetailService.updateBatchById(detailEntities);
        // 2.修改采购单的状态（需要遍历所有的采购项来决定）
        PurchaseEntity purchaseEntity = new PurchaseEntity();
        purchaseEntity.setId(purchaseDoneVo.getId());
        purchaseEntity.setAmount(price);
        purchaseEntity.setStatus(flag ? PurchaseStatusEnum.FINISHED.getCode() : PurchaseStatusEnum.HASERROR.getCode());
        this.updateById(purchaseEntity);
    }

}