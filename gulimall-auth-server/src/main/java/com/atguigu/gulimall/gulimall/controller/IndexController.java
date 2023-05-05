package com.atguigu.gulimall.gulimall.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * @author lxy
 * @version 1.0
 * @Description
 * @date 2023/5/5 13:59
 */
@Controller
public class IndexController {
    @GetMapping("/login.html")
    public String loginPage(){
        return "login";
    }

    @GetMapping("/register.html")
    public String registerPage(){
        return "register";
    }


}
