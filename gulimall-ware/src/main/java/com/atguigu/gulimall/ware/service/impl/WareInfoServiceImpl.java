package com.atguigu.gulimall.ware.service.impl;

import com.alibaba.fastjson.TypeReference;
import com.atguigu.common.constant.ware.StockLockStatusEnum;
import com.atguigu.common.to.mq.StockDetailTo;
import com.atguigu.common.to.mq.StockLockedTo;
import com.atguigu.common.utils.R;
import com.atguigu.gulimall.ware.entity.WareOrderTaskDetailEntity;
import com.atguigu.gulimall.ware.entity.WareOrderTaskEntity;
import com.atguigu.gulimall.ware.exception.NoStockException;
import com.atguigu.gulimall.ware.feign.MemberFeignService;
import com.atguigu.gulimall.ware.service.WareOrderTaskDetailService;
import com.atguigu.gulimall.ware.service.WareOrderTaskService;
import com.atguigu.gulimall.ware.vo.*;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.apache.commons.lang.StringUtils;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.BeanUtils;
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
import sun.plugin2.message.Message;

@RabbitListener(queues = "stock.release.stock.queue")
@Service("wareInfoService")
public class WareInfoServiceImpl extends ServiceImpl <WareInfoDao, WareInfoEntity> implements WareInfoService {

    @Autowired
    MemberFeignService memberFeignService;

    @Autowired
    RabbitTemplate rabbitTemplate;

    @Autowired
    WareOrderTaskDetailService wareOrderTaskDetailService;

    @Autowired
    WareOrderTaskService wareOrderTaskService;

    /**
     * 1、库存自动解锁：下订单成功，库存锁定成功，接下来的业务（积分、优惠券...）调用失败，导致订单回滚。===>锁定的库存就要自动解锁
     * 2、由于锁库存失败导致订单失败。===> 无需解锁库存
     * @param to
     * @param message
     */
    @RabbitHandler
    public void handleStockLockedRelease(StockLockedTo to, Message message){
        System.out.println("收到解锁库存的消息");
        Long id = to.getId();
        StockDetailTo detail = to.getDetail();
        Long detailId = detail.getId();
        /**
         * 解锁：
         * 查询数据库关于这个订单的锁定库存信息。
         * 有。库存锁定成功了，需要进行解锁
         * 无。库存锁定失败了，库存自动回滚了。无需解锁。
         */
        WareOrderTaskDetailEntity wareOrderTaskDetailEntity = wareOrderTaskDetailService.getById(detailId);
        if(wareOrderTaskDetailEntity!=null){
            // TODO 解锁
        }else {
            // 无需解锁
        }

    }

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
        } else {
            fareAndAddressVo.setFare(new BigDecimal(0));
        }
        fareAndAddressVo.setAddress(memberReceiveAddress);
        return fareAndAddressVo;
    }

    /**
     * 锁定订单中对应的商品库存：按照下单的收货地址，找到就近仓库，锁定库存
     *
     * @param vo
     * @return
     * @Transactional 默认不声明要回滚的异常的话，所有运行时异常都会回滚
     *
     * 库存解锁的场景
     * 1）、下订单成功，订单过期没有支付被系统自动取消、被用户手动取消。都要解锁库存
     * 2）、下订单成功，库存锁定成功，接下来的业务调用失败（积分扣减），导致订单回滚。之前锁定的库存就要自动解锁。
     *
     */
    @Transactional(rollbackFor = NoStockException.class)
    @Override
    public Boolean orderLockStock(WareSkuLockVo vo) {


        /**
         * 保存库存工作单的详情
         */
        WareOrderTaskEntity wareOrderTaskEntity = new WareOrderTaskEntity();
        wareOrderTaskEntity.setOrderSn(vo.getOrderSn());
        wareOrderTaskService.save(wareOrderTaskEntity);

        // 1、找到每个商品有库存的仓库列表
        List <OrderItemVo> lockItems = vo.getLockItems();
        if (lockItems == null || lockItems.size() == 0) {
            return false;
        }
        List <SkuWareHasStock> skuWareHasStocks = lockItems.stream().map(orderItem -> {
            SkuWareHasStock skuWareHasStock = new SkuWareHasStock();
            skuWareHasStock.setCount(orderItem.getCount());
            Long skuId = orderItem.getSkuId();
            // 查询该商品有库存的仓库
            List <Long> wareIds = this.baseMapper.listWareIdsHasSkuStock(skuId);
            skuWareHasStock.setWareId(wareIds);
            skuWareHasStock.setSkuId(skuId);
            return skuWareHasStock;
        }).collect(Collectors.toList());

        // 2、开始锁定库存
        for (SkuWareHasStock skuWareHasStock : skuWareHasStocks) {
            Boolean skuWareIsLock = false;
            Long skuId = skuWareHasStock.getSkuId();
            List <Long> wareIds = skuWareHasStock.getWareId();
            // 所有仓库中都没有，抛出异常【无需看后序其他商品是否可以锁库存成功，一件失败，全部失败！！！】
            if (wareIds == null || wareIds.size() == 0) {
                throw new NoStockException(skuId);
            }
            /**
             * 开始遍历所有的仓库，进行锁定，直到锁定成功，遍历结束
             * 结果分析：
             *      1、如果每一个商品都锁定成功，将当前商品锁定了几件的工作单记录发给MQ
             *      2、锁定失败。前面保存的工作单信息就回滚了。MQ发送出去的消息，即使需要解锁，由于去数据库查不到id，所以就不用解锁。（当做一个垃圾信息就行）
             */
            for (Long wareId : wareIds) {
                Long count = this.baseMapper.lockSkuStock(skuId, wareId, skuWareHasStock.getCount());
                if (count > 0) {
                    skuWareIsLock = true;
                    // 告知MQ库存锁定成功
                    WareOrderTaskDetailEntity wareOrderTaskDetail = new WareOrderTaskDetailEntity(null, skuId, null, skuWareHasStock.getCount(), wareOrderTaskEntity.getId(), wareId, StockLockStatusEnum.LOCKED.getCode());
                    wareOrderTaskDetailService.save(wareOrderTaskDetail);
                    StockLockedTo stockLockedTo = new StockLockedTo();
                    stockLockedTo.setId(wareOrderTaskEntity.getId());
                    StockDetailTo stockDetailTo = new StockDetailTo();
                    BeanUtils.copyProperties(wareOrderTaskDetail,stockDetailTo);
                    stockLockedTo.setDetail(stockDetailTo);
                    // 消息发出去了，当时这边回滚了。无需处理
                    rabbitTemplate.convertAndSend("stock-event-exchange","stock.locked",stockLockedTo);
                    break;
                }
            }
            if (!skuWareIsLock) {
                // 当前商品都没有锁定
                throw new NoStockException(skuId);
            }
        }
        return true;
    }

}