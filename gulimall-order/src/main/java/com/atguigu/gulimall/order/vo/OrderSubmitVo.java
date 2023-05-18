package com.atguigu.gulimall.order.vo;

import lombok.Data;

import java.math.BigDecimal;

/**
 * @author lxy
 * @version 1.0
 * @Description 封装订单提交的数据
 * @date 2023/5/18 14:28
 */
@Data
public class OrderSubmitVo {
    /**
     * 收货地址id
     */
    private Long addrId;
    /**
     * 支付方式【0-货到付款、1-在线支付】
     */
    private Integer payType = 1;
    /**
     * 防重令牌
     */
    private String orderToken;
    /**
     * 应付价格【当用户在PC上的订单确认页是两件商品，此时用户出去了一趟，在App上操作了一下又加了几件商品~当用户回到PC再次提交，到支付页面，此时价格肯定和之
     * 前确认订单页显示的不同，（这里进入支付其实还是要查询购物车中勾选的了）】
     */
    private BigDecimal payPrice;
    /**
     * 订单备注
     */
    private String note;
    // TODO 优惠、发票...
    // My NOTES 无需提交需要购买的商品，直接再去购物车再去获取一遍
}
