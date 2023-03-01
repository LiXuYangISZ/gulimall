package com.atguigu.gulimall.coupon;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

/**
 *1、如何使用Nacos作为配置中心统一管理配置
 * 1）、引入依赖
 * 2）、创建bootstrap.properties
 *  # 服务名称
 *  spring.application.name=gulimall-coupon
 *  # 配置中心地址
 *  spring.cloud.nacos.config.server-addr=192.168.127.128:8848
 * 3）、需要给配置中心默认添加一个叫 数据集（Data Id）gulimall-coupon.properties。默认规则：应用名.properties
 * 4）、给 应用名.properties 添加任何配置
 * 5）、动态获取配置
 *     @RefreshScrope：动态获取并刷新配置
 *     @Value("${配置项名}")：获取到配置
 *     如果配置中心和当前应用的配置文件中都配置了相同的项，优先使用配置中心的配置
 *
 * 2、细节
 *  1）、命名空间：配置隔离；
 *      默认：public(保留空间)； 默认新增的所有配置都在public空间
 *      ① 企业开发中经常利用命名空间来做环境隔离：开发、测试、生产
 *         注意：在bootstrap.properties;配置上，需要使用哪个命名空间下的配置。
 *         spring.cloud.nacos.config.namespace=空间ID
 *      ② 每一个微服务之间互相隔离配置，每一个微服务都创建自己的命名空间，只加载自己命名空间下的所有配置
 *  2）、配置集：所有配置的集合
 *
 *  3）、配置集ID：类似文件名
 *      Data ID：类似文件名
 *
 *  4）、默认分组：
 *      默认所有的配置集都属于：DEFAULT_GROUP
 *      比如不同的活动期间我们使用不同的分组：1111、618、1212
 *
 *    我们的项目采用的方案：每个微服务创建自己的命名空间，使用配置分组区分环境：dev、test、prod
 *
 * 3、同时加载多个配置集
 *  1）、微服务任何配置信息，任何配置文件都可以放在配置中心中
 *  2）、只需要bootstrap.properties说明加载配置中心中哪些配置文件即可
 *  3）、@Value,@ConfigurationProperties...以前SpringBoot任何从配置文件中获取值的方法，都可以使用
 *  4）、如果配置中心和当前应用的配置文件中都配置了相同的项，优先使用配置中心的配置
 *      bootstrap.yml->application.yml->spring.application.name-profile.yml
 *
 */
@EnableDiscoveryClient
@SpringBootApplication
public class GulimallCouponApplication {

    public static void main(String[] args) {
        SpringApplication.run(GulimallCouponApplication.class, args);
    }

}
