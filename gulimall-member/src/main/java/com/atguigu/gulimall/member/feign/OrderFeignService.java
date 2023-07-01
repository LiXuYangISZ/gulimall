package com.atguigu.gulimall.member.feign;

import com.atguigu.common.utils.PageUtils;
import com.atguigu.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Map;

/**
 * @author lxy
 * @version 1.0
 * @Description
 * @date 2023/7/1 16:53
 */
@FeignClient("gulimall-order")
public interface OrderFeignService {

    /**
     * 分页获取用户订单信息
     * @param params
     * @return
     */
    @PostMapping("/order/order/listWithItem")
    public R listWithItem(@RequestBody Map <String, Object> params);
}
