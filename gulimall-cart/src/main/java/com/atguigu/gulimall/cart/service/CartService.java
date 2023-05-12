package com.atguigu.gulimall.cart.service;

import com.atguigu.gulimall.cart.vo.Cart;
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

    /**
     * 获取购物车里面的商品
     * @return
     */
    Cart getCart() throws ExecutionException, InterruptedException;

    /**
     * 清空购物车
     * @param cartKey
     */
    void clearCart(String cartKey);

    /**
     * 勾选购物项
     * @param skuId
     * @param check
     */
    void checkItem(Long skuId, Integer check);
}
