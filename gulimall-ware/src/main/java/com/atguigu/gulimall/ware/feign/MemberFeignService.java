package com.atguigu.gulimall.ware.feign;

import com.atguigu.common.utils.R;
import com.atguigu.gulimall.ware.vo.MemberReceiveAddressVo;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

/**
 * @author lxy
 * @version 1.0
 * @Description
 * @date 2023/5/17 9:44
 */
@FeignClient("gulimall-member")
public interface MemberFeignService {
    /**
     * 根据地址id获取收货地址列表
     * @param id
     * @return
     */
    @RequestMapping("/member/memberreceiveaddress/info/{id}")
    R getReceiveAddressInfo(@PathVariable("id") Long id);
}
