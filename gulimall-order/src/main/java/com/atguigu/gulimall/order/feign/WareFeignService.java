package com.atguigu.gulimall.order.feign;

import com.atguigu.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

/**
 * @author lxy
 * @version 1.0
 * @Description
 * @date 2023/5/17 8:57
 */
@FeignClient("gulimall-ware")
public interface WareFeignService {
    /**
     * 查询sku列表的库存情况
     * @param skuIds
     * @return
     */
    @PostMapping("/ware/waresku/hasStock")
    R getSkusHasStock(@RequestBody List <Long> skuIds);

    /**
     * 获取运费信息
     * @param addrId
     * @return
     */
    @GetMapping("/ware/wareinfo/fare")
    R getFare(@RequestParam("addrId") Long addrId);
}
