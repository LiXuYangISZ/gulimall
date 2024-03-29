package com.atguigu.gulimall.order.service;

import com.atguigu.gulimall.order.vo.*;
import com.baomidou.mybatisplus.extension.service.IService;
import com.atguigu.common.utils.PageUtils;
import com.atguigu.gulimall.order.entity.OrderEntity;

import java.util.Map;
import java.util.concurrent.ExecutionException;

/**
 * 订单
 *
 * @author xuyang.li
 * @email xuyang.li@gmail.com
 * @date 2023-02-27 21:49:13
 */
public interface OrderService extends IService<OrderEntity> {

    PageUtils queryPage(Map<String, Object> params);

    /**
     * 确认订单
     * @return
     */
    OrderConfirmVo confirmOrder() throws ExecutionException, InterruptedException;

    /**
     * 提交订单
     * @param orderSubmitVo
     * @return
     */
    SubmitOrderResponseVo submitOrder(OrderSubmitVo orderSubmitVo);

    OrderEntity getOrderByOrderSn(String orderSn);

    /**
     * 关闭订单
     * @param entity
     */
    void closeOrder(OrderEntity entity);

    /**
     * 根据订单号获取支付详情信息
     * @param orderSn
     * @return
     */
    PayVo getOrderPay(String orderSn);

    /**
     * 分页获取用户的订单信息
     * @param params
     * @return
     */
    PageUtils queryPageWithItem(Map<String, Object> params);

    /**
     * 处理支付宝的支付结果
     * @param payAsyncVo
     * @return
     */
    String handlePayResult(PayAsyncVo payAsyncVo);
}

