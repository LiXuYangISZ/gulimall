package com.atguigu.gulimall.product;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

/**
 * 1、整合MyBatis-Plus
 *      1）、导入依赖
 *      2）、配置
 *          1、配置数据源
 *              1）、导入数据源驱动
 *              2）、在application.yml配置数据源相关信息
 *          2、配置MyBatis-Plus：
 *              1）、使用@MapperScan【@Mapper和@MapperScan两者使用一个就行，由于代码生成器生成的代码带着@Mapper，这里我们就无须次此注解了】
 */
@EnableDiscoveryClient
@SpringBootApplication
public class GulimallProductApplication {

    public static void main(String[] args) {
        SpringApplication.run(GulimallProductApplication.class, args);
    }

}
