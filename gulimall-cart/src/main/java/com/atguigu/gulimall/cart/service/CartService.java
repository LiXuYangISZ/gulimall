package com.atguigu.gulimall.cart.service;

import com.atguigu.gulimall.cart.vo.CartItem;

import java.util.concurrent.ExecutionException;

/**
 * @author lxy
 * @version 1.0
 * @Description
 * @date 2023/5/10 13:30
 */
public interface CartService {
    /**
     * 添加商品到购物车
     * @param skuId
     * @param count
     * @return
     * @throws ExecutionException
     * @throws InterruptedException
     */
    CartItem addToCart(Long skuId, Long count) throws ExecutionException, InterruptedException;

    /**
     * 根据skuId从Redis中查询商品信息
     * @param skuId
     * @return
     */
    CartItem getCartItem(Long skuId);
}
