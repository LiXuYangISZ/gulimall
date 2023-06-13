package com.atguigu.gulimall.ware.feign;

import com.atguigu.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * @author lxy
 * @version 1.0
 * @Description
 * @date 2023/6/13 20:58
 */
@FeignClient("gulimall-order")
public interface OrderFeignService {
    /**
     * 根据订单Sn获取订单详情
     * @param orderSn
     * @return
     */
    @GetMapping("/order/order/status/{orderSn}")
    R getOrderByOrderSn(@PathVariable("orderSn") String orderSn);
}
