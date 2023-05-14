package com.atguigu.gulimall.order.web;

import com.atguigu.gulimall.order.service.OrderService;
import com.atguigu.gulimall.order.vo.OrderConfirmVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * @author lxy
 * @version 1.0
 * @Description
 * @date 2023/5/14 17:36
 */
@Controller
public class OrderWebController {

    @Autowired
    OrderService orderService;

    /**
     * 跳转至确认订单页面
     * @param model
     * @return
     */
    @GetMapping("toTrade")
    public String toTrade(Model model){
        OrderConfirmVo orderConfirmVo = orderService.confirmOrder();
        model.addAttribute("orderConfirmData",orderConfirmVo);
        return "confirm";
    }
}
