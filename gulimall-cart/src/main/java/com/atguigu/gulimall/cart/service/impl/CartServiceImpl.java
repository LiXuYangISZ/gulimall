package com.atguigu.gulimall.cart.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.atguigu.common.constant.cart.CartConstant;
import com.atguigu.common.utils.R;
import com.atguigu.gulimall.cart.config.MyThreadPoolConfig;
import com.atguigu.gulimall.cart.feign.ProductFeignService;
import com.atguigu.gulimall.cart.interceptor.CartInterceptor;
import com.atguigu.gulimall.cart.service.CartService;
import com.atguigu.gulimall.cart.vo.Cart;
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
import java.util.stream.Collectors;

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

    @Override
    public CartItem getCartItem(Long skuId) {
        BoundHashOperations <String, Object, Object> cartOps = getCartOps();
        String cartItemStr = (String) cartOps.get(skuId.toString());
        CartItem cartItem = JSON.parseObject(cartItemStr, CartItem.class);
        return cartItem;
    }

    @Override
    public Cart getCart() throws ExecutionException, InterruptedException {
        Cart cart = new Cart();
        UserInfoTo userInfo = CartInterceptor.threadLocal.get();
        if (userInfo.getUserId() != null) {
            // 一、登录
            String tempCartKey = CartConstant.TEMP_USER_CART_PREFIX + userInfo.getUserKey();
            String loginCartKey = CartConstant.LOGIN_USER_CART_PREFIX + userInfo.getUserId();
            List <CartItem> tempCartItems = getCartItems(tempCartKey);
            if (tempCartItems != null && tempCartItems.size() > 0) {
                // 1.1 如果临时购物车有数据，则把临时购物车里面的商品添加至登录购物车里【合并购物车】
                for (CartItem tempCartItem : tempCartItems) {
                    addToCart(tempCartItem.getSkuId(), tempCartItem.getCount());
                }
                // 1.2 清空临时购物车
                clearCart(tempCartKey);
            }
            // 1.3 获取登录购物车的数据
            List <CartItem> loginCartItems = getCartItems(loginCartKey);
            cart.setCartItems(loginCartItems);
        } else {
            // 二、未登录，获取临时购物车的所有购物项
            List <CartItem> cartItems = getCartItems(CartConstant.TEMP_USER_CART_PREFIX + userInfo.getUserKey());
            if (cartItems != null) {
                cart.setCartItems(cartItems);
            }
        }
        return cart;
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

    /**
     * 获取购物车里面的所有购物项
     *
     * @param cartKey
     * @return
     */
    private List <CartItem> getCartItems(String cartKey) {
        BoundHashOperations <String, Object, Object> cartOps = redisTemplate.boundHashOps(cartKey);
        List <Object> values = cartOps.values();
        if (values != null && values.size() > 0) {
            List <CartItem> cartItems = values.stream().map((obj) -> JSON.parseObject(obj.toString(), CartItem.class)).collect(Collectors.toList());
            return cartItems;
        }
        return null;
    }

    /**
     * 清空购物车数据
     *
     * @param cartKey
     */
    @Override
    public void clearCart(String cartKey) {
        redisTemplate.delete(cartKey);
    }

    @Override
    public void checkItem(Long skuId, Integer check) {
        BoundHashOperations <String, Object, Object> cartOps = getCartOps();
        // 获取Redis中的cartItem
        CartItem cartItem = getCartItem(skuId);
        // 改变勾选状态
        cartItem.setCheck(check==1);
        String cartItemStr = JSON.toJSONString(cartItem);
        // 重新存进去
        cartOps.put(skuId.toString(),cartItemStr);
    }

    @Override
    public void changeItemCount(Long skuId, Long count) {
        BoundHashOperations <String, Object, Object> cartOps = getCartOps();
        // 获取Redis中的cartItem
        CartItem cartItem = getCartItem(skuId);
        // 改变勾选状态
        cartItem.setCount(count);
        String cartItemStr = JSON.toJSONString(cartItem);
        // 重新存进去
        cartOps.put(skuId.toString(),cartItemStr);
    }

    @Override
    public void deleteItem(Long skuId) {
        BoundHashOperations <String, Object, Object> cartOps = getCartOps();
        cartOps.delete(skuId.toString());
    }
}
