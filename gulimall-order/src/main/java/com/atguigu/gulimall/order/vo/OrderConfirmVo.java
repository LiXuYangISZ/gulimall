package com.atguigu.gulimall.order.vo;

import com.sun.xml.internal.bind.v2.TODO;
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
    List<CartItemVo> items;

    // TODO 发票记录... 可以按照搞张发票表，存放用户的发票信息。每次列出来，供用户进行选择
    // TODO 优惠券信息 可以查询coupon表中是否有符合条件的优惠券，如果有，列出来，供用户进行选择~
    /**
     * TODO 积分，用户可以勾选积分抵扣对应的价格~
     */
    Integer integration;

    /**
     * 获取订单总额
     * @return
     */
    public BigDecimal getTotal() {
        if(items!=null&&items.size() > 0){
            return items.stream().map(CartItemVo::getTotalPrice).reduce(BigDecimal::add).get();
        }else{
            return new BigDecimal(0);
        }
    }

    /**
     * TODO 防重令牌【因为用户提交订单这个处理时间比较长，然后用户可能会提交多次~ 从而造成错误】
     */
    String orderToken;

    /**
     * 获取应付价格
     */
    public BigDecimal getPayPrice() {
        return getTotal();
    }
}
