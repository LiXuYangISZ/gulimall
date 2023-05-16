package com.atguigu.gulimall.order.feign;

import com.atguigu.gulimall.order.vo.CartItemVo;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

/**
 * @author lxy
 * @version 1.0
 * @Description
 * @date 2023/5/15 23:21
 */
@FeignClient("gulimall-cart")
public interface CartFeignService {
    /**
     * 获取购物车中被选中的商品
     * @return
     */
    @GetMapping("/cart/currentUserCartItems")
    @ResponseBody
    List <CartItemVo> currentUserCartItems();
}
