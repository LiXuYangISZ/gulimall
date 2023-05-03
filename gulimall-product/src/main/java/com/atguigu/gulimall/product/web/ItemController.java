package com.atguigu.gulimall.product.web;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * @author lxy
 * @version 1.0
 * @Description
 * @date 2023/5/3 21:37
 */
@Controller
public class ItemController {
    @GetMapping("/{skuId}.html")
    public String skuItem(@PathVariable("skuId") Long skuId){
        System.out.println("准备查询"+skuId+"的详情~");
        return "item";
    }
}
