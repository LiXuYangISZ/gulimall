package com.atguigu.gulimall.cart.feign;

import com.atguigu.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.math.BigDecimal;
import java.util.List;

/**
 * @author lxy
 * @version 1.0
 * @Description 获取sku信息
 * @date 2023/5/10 23:14
 */
@FeignClient("gulimall-product")
public interface ProductFeignService {
    /**
     * 获取Sku信息
     */
    @RequestMapping("/product/skuinfo/info/{skuId}")
    R getSkuInfo(@PathVariable("skuId") Long skuId);


    /**
     * 获取Sku销售属性列表
     */
    @GetMapping("/product/skusaleattrvalue/stringList/{skuId}")
    List <String> getSkuSaleAttrValue(@PathVariable("skuId") Long skuId);

    /**
     * 获取商品的价格
     * @param skuId
     * @return
     */
    @GetMapping("/product/skuinfo/getPrice/{skuId}")
    BigDecimal getPrice(@PathVariable Long skuId);
}
