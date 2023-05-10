package com.atguigu.gulimall.cart.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * @author lxy
 * @version 1.0
 * @Description 购物项
 * @date 2023/5/10 12:37
 */
@Data
public class CartItem {
    /**
     * 商品skuId
     */
    private Long skuId;
    /**
     * 是否选中
     */
    private Boolean check = true;
    /**
     * 商品标题
     */
    private String title;
    /**
     * 默认图片
     */
    private String defaultImage;
    /**
     * 单价
     */
    private BigDecimal price;
    /**
     * 数量
     */
    private Long count;
    /**
     * 总价
     */
    private BigDecimal totalPrice;
    /**
     * 属性
     */
    private List<String> attrs;

    /**
     * 获取商品总价
     * @return
     */
    public BigDecimal getTotalPrice() {
        return price.multiply(new BigDecimal(count));
    }
}
