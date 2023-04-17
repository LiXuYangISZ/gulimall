package com.atguigu.common.to;

import lombok.Data;

/**
 * @author lxy
 * @version 1.0
 * @Description
 * @date 2023/4/17 16:33
 */
@Data
public class SkuHasStockTo {
    private Long skuId;
    private Boolean hasStock;
}
