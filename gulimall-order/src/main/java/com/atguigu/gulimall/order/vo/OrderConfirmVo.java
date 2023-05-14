package com.atguigu.gulimall.order.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * @author lxy
 * @version 1.0
 * @Description 订单确认页需要用的数据
 * @date 2023/5/14 22:49
 */
@Data
public class OrderConfirmVo {
    /**
     * 收获地址
     */
    List<MemberReceiveAddressVo> address;
    /**
     * 所有选中的购物项
     */
    List<OrderItemVo> items;

    // 发票记录...
    /**
     * 优惠券信息
     */
    Integer integration;
    /**
     * 订单总额
     */
    BigDecimal total;
    /**
     * 应付价格
     */
    BigDecimal payPrice;
}
