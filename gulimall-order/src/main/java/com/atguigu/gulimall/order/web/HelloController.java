package com.atguigu.gulimall.order.web;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * @author lxy
 * @version 1.0
 * @Description
 * @date 2023/5/14 15:39
 */
@Controller
public class HelloController {
    @GetMapping("/{page}.html")
    public String listPage(@PathVariable("page") String page){
        return page;
    }
}
