package com.atguigu.gulimall.order.vo;

import lombok.Data;

import java.math.BigDecimal;

/**
 * @author lxy
 * @version 1.0
 * @Description
 * @date 2023/5/17 10:43
 */
@Data
public class FareAndAddressVo {
    /**
     * 运费
     */
    private BigDecimal fare;
    /**
     * 地址信息
     */
    private MemberReceiveAddressVo address;
}
