package com.atguigu.gulimall.ware.service.impl;

import com.atguigu.common.constant.order.OrderStatusEnum;
import com.atguigu.common.constant.ware.StockLockStatusEnum;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.atguigu.common.utils.PageUtils;
import com.atguigu.common.utils.Query;

import com.atguigu.gulimall.ware.dao.WareOrderTaskDetailDao;
import com.atguigu.gulimall.ware.entity.WareOrderTaskDetailEntity;
import com.atguigu.gulimall.ware.service.WareOrderTaskDetailService;


@Service("wareOrderTaskDetailService")
public class WareOrderTaskDetailServiceImpl extends ServiceImpl<WareOrderTaskDetailDao, WareOrderTaskDetailEntity> implements WareOrderTaskDetailService {

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<WareOrderTaskDetailEntity> page = this.page(
                new Query<WareOrderTaskDetailEntity>().getPage(params),
                new QueryWrapper<WareOrderTaskDetailEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public List <WareOrderTaskDetailEntity> getLockedOrderTaskDetailByTaskId(Long id) {
        return this.baseMapper.
                selectList(new LambdaQueryWrapper <WareOrderTaskDetailEntity>()
                        .eq(WareOrderTaskDetailEntity::getTaskId, id)
                        .eq(WareOrderTaskDetailEntity::getLockStatus, StockLockStatusEnum.LOCKED.getCode()));
    }

}