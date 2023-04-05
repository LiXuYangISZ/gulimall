package com.atguigu.common.to;

import lombok.Data;

import java.math.BigDecimal;

/**
 * @author lxy
 * @version 1.0
 * @Description
 * @date 2023/4/5 13:58
 */
@Data
public class SpuBoundsTo {
    /**
     *
     */
    private Long spuId;
    /**
     * 成长积分
     */
    private BigDecimal growBounds;
    /**
     * 购物积分
     */
    private BigDecimal buyBounds;
    /**
     * 优惠生效情况[1111（四个状态位，从右到左）;0 - 无优惠，成长积分是否赠送;1 - 无优惠，购物积分是否赠送;2 - 有优惠，成长积分是否赠送;3 - 有优惠，购物积分是否赠送【状态位0：不赠送，1：赠送】]
     * TODO 扩展：可以加上优惠生效情况~
     */
    // private Integer work;
}
