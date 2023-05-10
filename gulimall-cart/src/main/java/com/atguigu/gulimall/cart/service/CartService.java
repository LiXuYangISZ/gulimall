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
    CartItem addToCart(Long skuId, Long count) throws ExecutionException, InterruptedException;
}
