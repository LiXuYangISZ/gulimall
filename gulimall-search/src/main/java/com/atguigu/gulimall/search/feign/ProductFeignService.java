package com.atguigu.gulimall.search.feign;

import com.atguigu.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * @author lxy
 * @version 1.0
 * @Description
 * @date 2023/5/2 16:06
 */
@FeignClient("gulimall-product")
public interface ProductFeignService {
    /**
     * 查询商品属性详情
     */
    @GetMapping("/product/attr/info/{attrId}")
    R getAttrInfo(@PathVariable("attrId") Long attrId);
}
