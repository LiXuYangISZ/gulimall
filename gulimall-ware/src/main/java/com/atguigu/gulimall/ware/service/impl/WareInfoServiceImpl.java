package com.atguigu.gulimall.ware.service.impl;

import com.alibaba.fastjson.TypeReference;
import com.atguigu.common.utils.R;
import com.atguigu.gulimall.ware.exception.NoStockException;
import com.atguigu.gulimall.ware.feign.MemberFeignService;
import com.atguigu.gulimall.ware.vo.*;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.atguigu.common.utils.PageUtils;
import com.atguigu.common.utils.Query;

import com.atguigu.gulimall.ware.dao.WareInfoDao;
import com.atguigu.gulimall.ware.entity.WareInfoEntity;
import com.atguigu.gulimall.ware.service.WareInfoService;
import org.springframework.transaction.annotation.Transactional;


@Service("wareInfoService")
public class WareInfoServiceImpl extends ServiceImpl <WareInfoDao, WareInfoEntity> implements WareInfoService {

    @Autowired
    MemberFeignService memberFeignService;

    @Override
    public PageUtils queryPage(Map <String, Object> params) {
        LambdaQueryWrapper <WareInfoEntity> queryWrapper = new LambdaQueryWrapper <>();
        String key = (String) params.get("key");
        if (StringUtils.isNotBlank(key)) {
            queryWrapper.eq(WareInfoEntity::getId, key).or()
                    .like(WareInfoEntity::getName, key).or()
                    .like(WareInfoEntity::getAddress, key).or()
                    .eq(WareInfoEntity::getAreacode, key);
        }
        IPage <WareInfoEntity> page = this.page(
                new Query <WareInfoEntity>().getPage(params),
                queryWrapper
        );

        return new PageUtils(page);
    }

    /**
     * 获取运费信息
     *
     * @param addrId
     * @return
     */
    @Override
    public FareAndAddressVo getFare(Long addrId) {
        // 远程调用会员表，获取用户的收货地址信息。根据仓库和收货地址的距离来计算运费~
        // TODO 可对接京东物流API或者快递100API
        R r = memberFeignService.getReceiveAddressInfo(addrId);
        FareAndAddressVo fareAndAddressVo = new FareAndAddressVo();
        MemberReceiveAddressVo memberReceiveAddress = r.getDataByName("memberReceiveAddress", new TypeReference <MemberReceiveAddressVo>() {
        });
        // 目前用手机号的最后一位作为运费
        String phone = memberReceiveAddress.getPhone();
        if (StringUtils.isNotBlank(phone)) {
            fareAndAddressVo.setFare(new BigDecimal(phone.substring(phone.length() - 1)));
        }else{
            fareAndAddressVo.setFare(new BigDecimal(0));
        }
        fareAndAddressVo.setAddress(memberReceiveAddress);
        return fareAndAddressVo;
    }

    /**
     * 锁定订单中对应的商品库存：按照下单的收货地址，找到就近仓库，锁定库存
     *
     * @Transactional 默认不声明要回滚的异常的话，所有运行时异常都会回滚
     * @param vo
     * @return
     */
    @Transactional(rollbackFor = NoStockException.class)
    @Override
    public Boolean orderLockStock(WareSkuLockVo vo) {
        // 1、找到每个商品有库存的仓库列表
        List <OrderItemVo> lockItems = vo.getLockItems();
        List <SkuWareHasStock> skuWareHasStocks = lockItems.stream().map(orderItem -> {
            SkuWareHasStock skuWareHasStock = new SkuWareHasStock();
            skuWareHasStock.setCount(orderItem.getCount());
            Long skuId = orderItem.getSkuId();
            // 查询该商品有库存的仓库
            List<Long> wareIds =  this.baseMapper.listWareIdsHasSkuStock(skuId);
            skuWareHasStock.setWareId(wareIds);
            return skuWareHasStock;
        }).collect(Collectors.toList());

        // 2、开始锁定库存
        for (SkuWareHasStock skuWareHasStock : skuWareHasStocks) {
            Boolean skuWareIsLock = false;
            Long skuId = skuWareHasStock.getSkuId();
            List <Long> wareIds = skuWareHasStock.getWareId();
             // 所有仓库中都没有，抛出异常【无需看后序其他商品是否可以锁库存成功，一件失败，全部失败！！！】
            if(wareIds == null || wareIds.size() == 0){
                throw new NoStockException(skuId);
            }
            // 开始遍历所有的仓库，进行锁定，知道锁定成功，遍历结束。
            for (Long wareId : wareIds) {
                Long count = this.baseMapper.lockSkuStock(skuId,wareId,skuWareHasStock.getCount());
                if(count>0){
                    skuWareIsLock = true;
                    break;
                }
            }
            if(!skuWareIsLock){
                // 当前商品都没有锁定
                throw new NoStockException(skuId);
            }
        }
        return true;
    }

}