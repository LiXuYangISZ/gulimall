package com.atguigu.gulimall.product.web;

import com.atguigu.gulimall.product.entity.CategoryEntity;
import com.atguigu.gulimall.product.service.CategoryService;
import com.atguigu.gulimall.product.vo.front.Catelog2Vo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;
import java.util.Map;

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
     * 查找一级分类
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

    /**
     * 查找对应的二级和三级分类
     * @ResponseBody：不需要页面跳转
     */
    @ResponseBody
    @GetMapping("index/json/catalog.json")
    public Map <String, List <Catelog2Vo>> getCatelogJson(){
        return categoryService.getCatelogJson();
    }

    @ResponseBody
    @GetMapping("/hello")
    public String hello(){
        return  "hello";
    }
}
