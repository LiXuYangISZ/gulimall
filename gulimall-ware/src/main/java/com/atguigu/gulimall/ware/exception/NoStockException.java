package com.atguigu.gulimall.ware.exception;

import lombok.Data;

/**
 * @author lxy
 * @version 1.0
 * @Description 库存不足异常
 * @date 2023/5/23 18:39
 */
@Data
public class NoStockException extends RuntimeException{
    private Long skuId;

    public NoStockException(Long skuId) {
        super("商品id："+skuId+"没有足够的库存了");
    }
}
