package com.atguigu.gulimall.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * @author lxy
 * @version 1.0
 * @Description 视图配置器
 * @date 2023/5/5 16:07
 */
@Configuration
public class GulimallWebConfig implements WebMvcConfigurer {

    /**
     * 视图映射,配置路径和视图的对应关系
     * @param registry
     */
    @Override
    public void addViewControllers(ViewControllerRegistry registry) {
        registry.addViewController("/login.html").setViewName("login");
        registry.addViewController("/register.html").setViewName("register");
    }
}
