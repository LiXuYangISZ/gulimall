package com.atguigu.gulimall.cart.controller;

import com.atguigu.gulimall.cart.interceptor.CartInterceptor;
import com.atguigu.gulimall.cart.vo.UserInfoTo;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * @author lxy
 * @version 1.0
 * @Description
 * 浏览器有一个cookie：user-key；标识用户身份，一个月后过期
 * 如果第一次使用JD的购物车功能，都会给一个临时的用户身份（这个和登录状态无关，是一定要分配的。这样我通过这个标识即使不登录依然可以保存用户的一些操作信息，对用户十分有好）
 * 浏览器对于用户操作的保存，每次访问都会携带者这个cookie
 *
 * 登录状态：Redis的session有值，按照userid来做（加入购物车、查看购物车）
 * 未登录：按照cookie中的user-key来做（加入购物车、查看购物车）
 * 第一次打开网站，因为没有临时用户，需要创建一个临时用户~【通过Interceptor实现】
 *
 * @date 2023/5/10 14:08
 */
@Controller
public class CartController {
    @GetMapping("/cartList.html")
    public String cartListPage(){
        // 快速得到用户信息
        UserInfoTo userInfo = CartInterceptor.threadLocal.get();
        System.out.println(userInfo);
        return "cartList";
    }

    /**
     * 添加商品到购物车
     * @return
     */
    @GetMapping("/addToCart")
    public String addToCart(){
        return "success";
    }
}
