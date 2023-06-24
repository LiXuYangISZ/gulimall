package com.atguigu.gulimall.ware.service.impl;

import com.alibaba.fastjson.TypeReference;
import com.atguigu.common.constant.order.OrderStatusEnum;
import com.atguigu.common.constant.ware.StockLockStatusEnum;
import com.atguigu.common.to.mq.OrderTo;
import com.atguigu.common.to.mq.StockDetailTo;
import com.atguigu.common.to.mq.StockLockedTo;
import com.atguigu.common.utils.R;
import com.atguigu.gulimall.ware.entity.WareOrderTaskDetailEntity;
import com.atguigu.gulimall.ware.entity.WareOrderTaskEntity;
import com.atguigu.gulimall.ware.exception.NoStockException;
import com.atguigu.gulimall.ware.feign.OrderFeignService;
import com.atguigu.gulimall.ware.feign.ProductFeignService;
import com.atguigu.common.to.SkuHasStockTo;
import com.atguigu.gulimall.ware.service.WareOrderTaskDetailService;
import com.atguigu.gulimall.ware.service.WareOrderTaskService;
import com.atguigu.gulimall.ware.vo.OrderItemVo;
import com.atguigu.gulimall.ware.vo.OrderVo;
import com.atguigu.gulimall.ware.vo.SkuWareHasStock;
import com.atguigu.gulimall.ware.vo.WareSkuLockVo;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.apache.commons.lang.StringUtils;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.BeanUtils;
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
import org.springframework.transaction.annotation.Transactional;


@Service("wareSkuService")
public class WareSkuServiceImpl extends ServiceImpl <WareSkuDao, WareSkuEntity> implements WareSkuService {

    @Autowired
    ProductFeignService productFeignService;

    @Autowired
    RabbitTemplate rabbitTemplate;


    @Autowired
    WareOrderTaskDetailService wareOrderTaskDetailService;

    @Autowired
    WareOrderTaskService wareOrderTaskService;

    @Autowired
    OrderFeignService orderFeignService;

    /**
     * 解锁库存
     *
     * @param detail
     */
    @Transactional
    public void unLockStock(StockDetailTo detail) {
        // 恢复库存
        this.baseMapper.unLockStock(detail.getSkuId(), detail.getWareId(), detail.getSkuNum().intValue());
        // 修改锁定状态
        WareOrderTaskDetailEntity wareOrderTaskDetailEntity = new WareOrderTaskDetailEntity();
        wareOrderTaskDetailEntity.setId(detail.getId());
        wareOrderTaskDetailEntity.setLockStatus(StockLockStatusEnum.UNLOCKED.getCode());
        wareOrderTaskDetailService.updateById(wareOrderTaskDetailEntity);
    }

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
                Map <String, Object> data = (Map <String, Object>) r.get("skuInfo");
                if (r.getCode() == 0) {
                    wareSkuEntity.setSkuName((String) data.get("skuName"));
                }
            } catch (Exception e) {

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

    /**
     * 锁定订单中对应的商品库存：按照下单的收货地址，找到就近仓库，锁定库存
     *
     * @param vo
     * @return
     * @Transactional 默认不声明要回滚的异常的话，所有运行时异常都会回滚
     * <p>
     * 库存解锁的场景
     * 1）、下订单成功，订单过期没有支付被系统自动取消、被用户手动取消。都要解锁库存
     * 2）、下订单成功，库存锁定成功，接下来的业务调用失败（积分扣减），导致订单回滚。之前锁定的库存就要自动解锁。
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
                    BeanUtils.copyProperties(wareOrderTaskDetail, stockDetailTo);
                    stockLockedTo.setDetail(stockDetailTo);
                    // 消息发出去了，当时这边回滚了。无需处理
                    rabbitTemplate.convertAndSend("stock-event-exchange", "stock.locked", stockLockedTo);
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

    /**
     * 解锁库存，对立面的符合条件的商品库存进行解锁
     * 1、库存自动解锁：下订单成功，库存锁定成功，接下来的业务（积分、优惠券...）调用失败，导致订单回滚。===>锁定的库存就要自动解锁
     * 2、由于锁库存失败导致订单失败。===> 无需解锁库存
     *
     * @param to
     */
    @Override
    public void unLockStock(StockLockedTo to) {
        Long id = to.getId();
        StockDetailTo detail = to.getDetail();
        Long detailId = detail.getId();
        /**
         * 解锁：查询数据库关于这个订单的锁定库存信息。
         * 有。说明库存锁定成功了，需要判断订单情况
         *      1、没有这个订单，说明后序其他流程出错（扣减积分、优惠信息），导致订单回滚了，必须解锁库存。
         *      2、有这个订单。需要需要判断订单状态，如果已经取消（手动取消、超时未支付被动取消），需要解锁库存
         * 无。库存锁定失败了，库存自动回滚了。这种情况无需解锁。
         */
        WareOrderTaskDetailEntity wareOrderTaskDetailEntity = wareOrderTaskDetailService.getById(detailId);
        if (wareOrderTaskDetailEntity != null) {
            WareOrderTaskEntity taskEntity = wareOrderTaskService.getById(to.getId());
            R r = orderFeignService.getOrderByOrderSn(taskEntity.getOrderSn());
            if (r.getCode() == 0) {
                OrderVo order = r.getData(new TypeReference <OrderVo>() {
                });
                // MY NOTES 如果没有这个订单或者订单已经被取消，则解锁
                //  这样可以保证，解锁库存不能在订单变为取消支付前执行成功
                if (order == null || OrderStatusEnum.CANCLED.getCode().equals(order.getStatus())) {
                    // 只有是锁定状态才可以进行解锁
                    if (StockLockStatusEnum.LOCKED.getCode().equals(wareOrderTaskDetailEntity.getLockStatus())) {
                        unLockStock(detail);
                    }
                } else {
                    // 本次下单成功...
                }
            } else {
                // 调用失败，则把消息拒绝重新放回队列里面，让别人继续消费解锁
                throw new RuntimeException("网络拥堵，远程调用Order服务失败~");
            }
        } else {
            // 已经回滚了，无需解锁~
        }
    }

    /**
     * 订单取消成功后，库存进行主动解锁
     * 1、订单取消成功，就应该对库存进行释放。不然就会导致订单一直下失败，但是库存一直在扣减。  虽然我们写了被动解锁库存的功能，但是要明白 --- 主动解锁库存是主要的逻辑，被动解锁库存是作为补偿！！！
     * 2、防止订单服务卡顿，导致订单状态信息一直修改不了，库存消息优先到期。查看订单状态是新建状态，什么也不做就走了。从而导致卡顿的订单，永远不能解锁库存
     *
     * @param to
     */
    @Transactional
    @Override
    public void unLockStock(OrderTo to) {
        String orderSn = to.getOrderSn();
        WareOrderTaskEntity wareOrderTask = wareOrderTaskService.getOrderTaskBySn(orderSn);
        if (wareOrderTask != null) {
            List <WareOrderTaskDetailEntity> wareOrderTaskDetailEntities = wareOrderTaskDetailService.getLockedOrderTaskDetailByTaskId(wareOrderTask.getId());
            if (wareOrderTaskDetailEntities != null && wareOrderTaskDetailEntities.size() > 0) {
                for (WareOrderTaskDetailEntity wareOrderTaskDetailEntity : wareOrderTaskDetailEntities) {
                    StockDetailTo stockDetailTo = new StockDetailTo();
                    BeanUtils.copyProperties(wareOrderTaskDetailEntity, stockDetailTo);
                    unLockStock(stockDetailTo);
                }
            }
        }
    }
}