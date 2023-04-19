package com.atguigu.gulimall.product.web;

import com.atguigu.gulimall.product.entity.CategoryEntity;
import com.atguigu.gulimall.product.service.CategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

/**
 * @author lxy
 * @version 1.0
 * @Description
 * @date 2023/4/19 16:41
 */
@Controller
public class IndexController {

    @Autowired
    CategoryService categoryService;

    /**
     *
     * @param model
     * @return
     */
    @GetMapping({"/","index.html"})
    public String indexPage(Model model){
        // 查出所有的一级分类
        List<CategoryEntity> categoryEntities = categoryService.getLevelOneCategorys();
        model.addAttribute("categorys",categoryEntities);
        //视图解析器进行拼接跳转路径:classpath:/templates/+返回值+ .html
        return "index";
    }
}
