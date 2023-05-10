package com.atguigu.gulimall.cart.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * @author lxy
 * @version 1.0
 * @Description 购物车
 * 需要计算的属性必须重写他的get方法，保证每次获取属性都会进行计算【如果用户自定义get/set方法了，lombok会默认采用的】
 * 购物车不存在线程安全问题哦~
 * @date 2023/5/10 12:37
 */
@Data
public class Cart {
    List <CartItem> cartItems;
    /**
     * 商品数量
     */
    private Long countNum;
    /**
     * 类型数量
     */
    private Long countType;
    /**
     * 商品总价格
     */
    private BigDecimal totalAmount;
    /**
     * 减免价格
     */
    private BigDecimal reduce = new BigDecimal(0);

    public Long getCountNum() {
        return cartItems == null ? 0L : cartItems.stream().filter(CartItem::getCheck).map(CartItem::getCount).reduce(Long::sum).get();
    }


    public Long getCountType() {
        return cartItems == null ? 0 : cartItems.stream().filter(CartItem::getCheck).count();
    }


    public BigDecimal getTotalAmount() {
        // TODO 减掉减免价格
        return cartItems == null ?
               new BigDecimal(0L) :
               cartItems.stream().filter(CartItem::getCheck).map(cartItem -> cartItem.getPrice().multiply(new BigDecimal(cartItem.getCount()))).reduce(BigDecimal::add).get();
    }
}
