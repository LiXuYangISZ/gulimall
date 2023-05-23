package com.atguigu.gulimall.order.vo;

import lombok.Data;

import java.util.List;

/**
 * @author lxy
 * @version 1.0
 * @Description 锁定库存的订单项信息
 * @date 2023/5/23 16:14
 */
@Data
public class WareSkuLockVo {
    /**
     * 订单号
     */
    private String orderSn;
    /**
     * 需要锁定的订单项列表
     */
    private List<OrderItemVo> lockItems;
}
