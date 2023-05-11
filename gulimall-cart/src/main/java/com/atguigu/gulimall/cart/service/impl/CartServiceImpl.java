package com.atguigu.gulimall.cart.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.atguigu.common.constant.cart.CartConstant;
import com.atguigu.common.utils.R;
import com.atguigu.gulimall.cart.config.MyThreadPoolConfig;
import com.atguigu.gulimall.cart.feign.ProductFeignService;
import com.atguigu.gulimall.cart.interceptor.CartInterceptor;
import com.atguigu.gulimall.cart.service.CartService;
import com.atguigu.gulimall.cart.vo.CartItem;
import com.atguigu.gulimall.cart.vo.SkuInfoTo;
import com.atguigu.gulimall.cart.vo.UserInfoTo;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.BoundHashOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * @author lxy
 * @version 1.0
 * @Description
 * @date 2023/5/10 13:30
 */
@Slf4j
@Service
public class CartServiceImpl implements CartService {
    @Autowired
    StringRedisTemplate redisTemplate;

    @Autowired
    ProductFeignService productFeignService;

    @Autowired
    ThreadPoolExecutor executor;

    @Override
    public CartItem addToCart(Long skuId, Long count) throws ExecutionException, InterruptedException {
        BoundHashOperations <String, Object, Object> cartOps = getCartOps();
        String oldCartItemStr = (String) cartOps.get(skuId.toString());
        // 如果购物车中还没有这个商品，则直接添加
        if (StringUtils.isBlank(oldCartItemStr)) {
            CartItem cartItem = new CartItem();
            // 1、远程查询当前待添加商品的信息
            CompletableFuture <Void> getSkuInfoFuture = CompletableFuture.runAsync(() -> {
                R r = productFeignService.getSkuInfo(skuId);
                if (r.getCode() == 0) {
                    SkuInfoTo skuInfo = r.getDataByName("skuInfo", new TypeReference <SkuInfoTo>() {
                    });
                    cartItem.setCheck(true);
                    cartItem.setCount(count);
                    cartItem.setDefaultImage(skuInfo.getSkuDefaultImg());
                    cartItem.setPrice(skuInfo.getPrice());
                    cartItem.setSkuId(skuId);
                    cartItem.setTitle(skuInfo.getSkuTitle());
                }
            }, executor);
            // 2、远程查询当前商品的销售信息
            CompletableFuture <Void> getSkuSaleValuesFuture = CompletableFuture.runAsync(() -> {
                List <String> skuSaleAttrValue = productFeignService.getSkuSaleAttrValue(skuId);
                cartItem.setAttrs(skuSaleAttrValue);
            }, executor);

            CompletableFuture.allOf(getSkuInfoFuture, getSkuSaleValuesFuture).get();
            // 3、保存至Redis
            cartOps.put(skuId.toString(), JSON.toJSONString(cartItem));
            return cartItem;
        } else {
            // 如果购物车中已经有这个商品的数据了，则进行修改后覆盖
            CartItem cartItem = JSON.parseObject(oldCartItemStr, CartItem.class);
            cartItem.setCount(cartItem.getCount() + count);
            cartOps.put(skuId.toString(), JSON.toJSONString(cartItem));
            return cartItem;
        }
    }

    /**
     * 获取到我们要操作的购物车OPS
     * 使用BoundHashOperations相比传统的opsForHash，可以少传key.
     *
     * @return
     */
    private BoundHashOperations <String, Object, Object> getCartOps() {
        UserInfoTo userInfo = CartInterceptor.threadLocal.get();
        String cartKey = "";
        if (userInfo.getUserId() != null) {
            // 用户已经登录
            cartKey = CartConstant.LOGIN_USER_CART_PREFIX + userInfo.getUserId();
        } else {
            // 未登录
            cartKey = CartConstant.TEMP_USER_CART_PREFIX + userInfo.getUserKey();
        }
        return redisTemplate.boundHashOps(cartKey);
    }
}
