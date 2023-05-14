package com.atguigu.gulimall.order.web;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * @author lxy
 * @version 1.0
 * @Description
 * @date 2023/5/14 17:36
 */
@Controller
public class OrderWebController {
    @GetMapping("toTrade")
    public String toTrade(){
        return "confirm";
    }
}
