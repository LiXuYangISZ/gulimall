package com.atguigu.gulimall.ware.vo;

import lombok.Data;

/**
 * @author lxy
 * @version 1.0
 * @Description 锁库存的返回结果
 * @date 2023/5/23 16:54
 */
@Data
public class LockStockResult {
    /**
     * skuId
     */
    private Long skuId;
    /**
     * 数量
     */
    private Integer count;
    /**
     * 是否锁定成功
     */
    private Boolean locked;
}
