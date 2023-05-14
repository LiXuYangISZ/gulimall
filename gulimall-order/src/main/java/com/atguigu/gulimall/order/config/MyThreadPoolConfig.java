package com.atguigu.gulimall.order.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * @author lxy
 * @version 1.0
 * @Description 项目的线程池
 * @date 2023/5/5 11:19
 */
// @EnableConfigurationProperties(ThreadPoolConfigProperties.class) //也可以采用这种方式加载配置类(SpringBoot采用的就是在这种)
@Configuration
public class MyThreadPoolConfig {
    @Bean
    public ThreadPoolExecutor threadPoolExecutor(ThreadPoolConfigProperties threadPool){
        return new ThreadPoolExecutor(threadPool.getCorePoolSize(),threadPool.getMaximumPoolSize(),threadPool.getKeepAliveTime(), threadPool.getUnit(),new LinkedBlockingDeque <>(100000), Executors.defaultThreadFactory(),new ThreadPoolExecutor.AbortPolicy());
    }
}
