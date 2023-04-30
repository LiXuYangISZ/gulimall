package com.atguigu.gulimall.search.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * @author lxy
 * @version 1.0
 * @Description
 * @date 2023/4/30 10:01
 */
@Controller
public class SearchController {

    @GetMapping("/list.html")
    public String listPage(){
        return "list";
    }
}
