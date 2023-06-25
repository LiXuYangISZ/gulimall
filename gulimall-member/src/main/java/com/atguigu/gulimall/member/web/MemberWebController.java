package com.atguigu.gulimall.member.web;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * @author lxy
 * @version 1.0
 * @Description
 * @date 2023/6/25 17:19
 */
@Controller
public class MemberWebController {

    @GetMapping("/memberOrder.html")
    public String memberOrderPage(){
        /**
         * 查询当前登录用户的所有订单列表数据
         */
        return "orderList";
    }
}
