package com.atguigu.common.constant.cart;

/**
 * @author lxy
 * @version 1.0
 * @Description
 * @date 2023/5/10 15:34
 */
public class CartConstant {
    /**
     * 未登录用户【临时用户】的临时key，用于标识用户
     */
    public static final String TEMP_USER_COOKIE_NAME = "user-key";

    /**
     * 临时用户的user_key的过期时间【一个月】
     */
    public static final Integer TEMP_USER_COOKIE_TIMEOUT = 60*60*24*30;
    /**
     * 临时用户的购物车数据key前缀
     */
    public static final String TEMP_USER_CART_PREFIX = "cart:temp:";
    /**
     * 登录用户的购物车数据key前缀
     */
    public static final String LOGIN_USER_CART_PREFIX = "cart:login:";
}
