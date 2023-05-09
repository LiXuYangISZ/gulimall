package com.atguigu.gulimall.product.config;

import com.alibaba.fastjson.support.spring.GenericFastJsonRedisSerializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.session.web.http.CookieSerializer;
import org.springframework.session.web.http.DefaultCookieSerializer;

/**
 * @author lxy
 * @version 1.0
 * @Description https://docs.spring.io/spring-session/reference/2.7/samples.html
 * @date 2023/5/9 17:22
 */
@Configuration
public class GulimallSessionConfig {
    @Bean
    public CookieSerializer cookieSerializer(){
        DefaultCookieSerializer cookieSerializer = new DefaultCookieSerializer();
        // 作用域
        cookieSerializer.setDomainName("gulimall.com");
        // 名称
        cookieSerializer.setCookieName("GULISESSION");
        return cookieSerializer;
    }

    @Bean
    public RedisSerializer <Object> springSessionDefaultRedisSerializer() {
        return new GenericFastJsonRedisSerializer();
    }
}
