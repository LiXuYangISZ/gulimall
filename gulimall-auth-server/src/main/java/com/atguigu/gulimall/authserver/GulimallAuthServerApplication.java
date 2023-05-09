package com.atguigu.gulimall.authserver;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.session.data.redis.config.annotation.web.http.EnableRedisHttpSession;
/**
 * SpringSession 核心原理
 * 1)、@EnableRedisHttpSession导入RedisHttpSessionConfiguration配置
 *      1、给容器中添加了一个组件
 *          SessionRepository = 》》》【RedisOperationsSessionRepository】==》用途：redis操作session进行增删改查的封装类
 *      2、SessionRepositoryFilter == 》Filter： session存储过滤器；每个请求过来都必须经过filter
 *          1、创建的时候，就自动从容器中获取到了SessionRepository；
 *          2、原始的request，response都被包装。SessionRepositoryRequestWrapper，SessionRepositoryResponseWrapper
 *          3、以后获取session。request.getSession();
 *          //SessionRepositoryRequestWrapper
 *          4、wrappedRequest.getSession();===> SessionRepository 中获取到的。
 *
 *  核心：装饰者模式；使用装饰者进行了包装，重写了getSession和setSession方法，让其都是使用SessionRepository来进行操作Session的~
 *
 *  SpringSession还实现了一些常见的功能：自动延期；redis中的数据也是有过期时间，一旦用户关闭浏览器就删除session。
 *
 *
 *
 *
 */
@EnableRedisHttpSession// 整合redis作为session存储
@EnableFeignClients
@EnableDiscoveryClient
@SpringBootApplication
public class GulimallAuthServerApplication {

    public static void main(String[] args) {
        SpringApplication.run(GulimallAuthServerApplication.class, args);
    }

}
