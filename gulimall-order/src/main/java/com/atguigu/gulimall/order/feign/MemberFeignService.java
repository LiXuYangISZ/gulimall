package com.atguigu.gulimall.order.feign;

import com.atguigu.gulimall.order.vo.MemberReceiveAddressVo;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

/**
 * @author lxy
 * @version 1.0
 * @Description
 * @date 2023/5/14 23:12
 */
@FeignClient("gulimall-member")
public interface MemberFeignService {
    /**
     * 根据用户id获取收货地址列表
     * @param memberId
     * @return
     */
    @GetMapping("member/memberreceiveaddress/{memberId}/addresses")
    List <MemberReceiveAddressVo> getAddress(@PathVariable Long memberId);
}
