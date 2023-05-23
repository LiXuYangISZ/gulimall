package com.atguigu.gulimall.order.vo;

import com.atguigu.gulimall.order.entity.OrderEntity;
import com.atguigu.gulimall.order.entity.OrderItemEntity;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * @author lxy
 * @version 1.0
 * @Description 订单创建封装的Vo【需要插入库】
 * 不同于OrderSubmitVo，因为里面的商品信息可能是不准确的~
 * @date 2023/5/22 18:13
 */
@Data
public class OrderCreateVo {
    /**
     * 订单信息
     */
    private OrderEntity order;
    /**
     * 订单项列表
     */
    private List <OrderItemEntity> orderItems;
}
