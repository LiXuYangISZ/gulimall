package com.atguigu.gulimall.order.vo;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

/**
 * @author lxy
 * @version 1.0
 * @Description 购物车服务返回的订单项Vo
 * @date 2023/5/14 22:56
 */
@Data
public class OrderItemVo implements Serializable {
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
     * TODO 查询商品重量【咱们这个项目实在SPU维度，其实实际上应该是SKU维度。比如15.6寸笔记本和14寸笔记本重量是不一样的哦】
     */
    private BigDecimal weight;

    // /**
    //  * 获取商品总价
    //  * @return
    //  */
    // public BigDecimal getTotalPrice() {
    //     return price.multiply(new BigDecimal(count));
    // }
}
