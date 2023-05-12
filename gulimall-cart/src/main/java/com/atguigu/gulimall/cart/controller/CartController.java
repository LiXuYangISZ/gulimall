package com.atguigu.gulimall.cart.controller;

import com.atguigu.gulimall.cart.interceptor.CartInterceptor;
import com.atguigu.gulimall.cart.service.CartService;
import com.atguigu.gulimall.cart.vo.Cart;
import com.atguigu.gulimall.cart.vo.CartItem;
import com.atguigu.gulimall.cart.vo.UserInfoTo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.concurrent.ExecutionException;

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

    @Autowired
    CartService cartService;

    @GetMapping("/cartList.html")
    public String cartListPage(Model model) throws ExecutionException, InterruptedException {
        // 快速得到用户信息
        Cart cart = cartService.getCart();
        model.addAttribute("cart",cart);
        return "cartList";
    }

    /**
     * 添加商品到购物车，并重定向到成功页面
     * 如何重定向的路径后面携带参数呢？
     * 1、RedirectAttributes redirect
     *      redirect.addFlashAttribute();将数据放在session里面，可以在页面中取出，但是只能取一次。
     *      redirect.addAttribute("skuId",skuId);将数据放在url后面
     * 2、直接拼接的方式?skuId=48&count=100
     * @return
     */
    @GetMapping("/addToCart")
    public String addToCart(@RequestParam("skuId") Long skuId, @RequestParam("count") Long count,RedirectAttributes redirectAttributes) throws ExecutionException, InterruptedException {
        cartService.addToCart(skuId,count);
        redirectAttributes.addAttribute("skuId",skuId);
        // MY NOTES 由于转发过去地址不变,就会导致用户不停的刷新,会不断的向购物车中添加数据
        //  思路: 改为重定向到页面(因为重定向会修改路径),然后把skuId传过去,每次刷新从新从Redis中获取数据
        return "redirect:http://cart.gulimall.com/addToCartSuccess.html";
    }

    /**
     * 查询添加成功的商品信息
     * @param skuId
     * @param model
     * @return
     */
    @GetMapping("/addToCartSuccess.html")
    public String addToCartSuccessPage(@RequestParam("skuId") Long skuId,Model model){
        CartItem item = cartService.getCartItem(skuId);
        model.addAttribute("cartItem",item);
        return "success";
    }
}
