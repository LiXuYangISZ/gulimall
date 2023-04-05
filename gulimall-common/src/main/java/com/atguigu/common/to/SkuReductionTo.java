package com.atguigu.common.to;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * @author lxy
 * @version 1.0
 * @Description
 * @date 2023/4/5 15:12
 */
@Data
public class SkuReductionTo {
    private Long skuId;
    private Integer fullCount;
    private BigDecimal discount;
    private Integer countStatus;
    private BigDecimal fullPrice;
    private BigDecimal reducePrice;
    private BigDecimal price;
    private Integer priceStatus;
    private List <MemberPrice> memberPrice;
}

