package com.atguigu.gulimall.member.config;

import com.atguigu.gulimall.member.interceptor.LoginInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * @author lxy
 * @version 1.0
 * @Description SpringMVC配置类
 * @date 2023/5/14 17:44
 */
@Configuration
public class MemberWebConfiguration implements WebMvcConfigurer {

    @Autowired
    LoginInterceptor loginInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(loginInterceptor).addPathPatterns("/**").excludePathPatterns("/member/**");
    }
}
