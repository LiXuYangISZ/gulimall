package com.atguigu.gulimall.ware.feign;

import com.atguigu.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * @author lxy
 * @version 1.0
 * @Description
 * @date 2023/4/10 16:57
 */
@FeignClient("gulimall-product")
public interface ProductFeignService {
    /**
     * 1）、让所有请求过网关：
     *      1、@FeignClient("gulimall-gateway")：给gulimall-gateway所在的机器发送请求
     *      2、请求路径：/api/product/skuinfo/info/{skuId}
     * 2）、直接让后台指定服务处理
     *      1、@FeignClient("gulimall-product")
     *      2、请求路径：/product/skuinfo/info/{skuId}
     *
     *
     * 获取sku基本信息
     */
    @RequestMapping("/product/skuinfo/info/{skuId}")
    R getSkuInfo(@PathVariable("skuId") Long skuId);
}
