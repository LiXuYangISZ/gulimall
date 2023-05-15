package com.atguigu.gulimall.order.vo;

import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

/**
 * @author lxy
 * @version 1.0
 * @Description
 * @date 2023/5/14 22:56
 */
@Data
public class CartItemVo implements Serializable {
    private static final long serialVersionUID = 1L;

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
    private List <String> attrs;

    /**
     * 获取商品总价
     * @return
     */
    public BigDecimal getTotalPrice() {
        return price.multiply(new BigDecimal(count));
    }
}
