package com.atguigu.gulimall.order.feign;

import com.atguigu.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * @author lxy
 * @version 1.0
 * @Description
 * @date 2023/5/23 11:30
 */
@FeignClient("gulimall-product")
public interface ProductFeignService {
    /**
     * 根据skuId获取Spu信息
     * @param skuId
     * @return
     */
    @GetMapping("/product/spuinfo/{skuId}/info")
    R getSpuInfoBySkuId(@PathVariable("skuId") Long skuId);
}
