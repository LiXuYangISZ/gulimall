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
     * TODO 应付价格 --- 验价【当用户在PC上的订单确认页是两件商品，此时用户打开手机APP，又在购物车勾选了几个商品。当用户回到PC再次提交，到支付页面，此时价格肯定和之
     *  前确认订单页显示的不同（因为商品每次到支付页会再次查找一遍），然后用户就会感到很奇怪，之前不是A价格么，咋变成B了呢？？？
     *  解决办法：每次带着订单确认页的价格到支付页，如果价格不同，那么就重定向到确认页，给用户个提示，并进行更新商品信息
     */
    private BigDecimal payPrice;
    /**
     * 订单备注【放两双筷子，少放辣椒】
     */
    private String note;
    // TODO 优惠、发票...
    // My NOTES 无需提交需要购买的商品，直接再去购物车再去获取一遍【获取选中的那些】
}
