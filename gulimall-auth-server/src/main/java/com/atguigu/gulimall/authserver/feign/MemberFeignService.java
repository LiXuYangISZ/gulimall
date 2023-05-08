package com.atguigu.gulimall.authserver.feign;

import com.atguigu.common.utils.R;
import com.atguigu.gulimall.authserver.vo.SocialWeiBoAuthInfo;
import com.atguigu.gulimall.authserver.vo.UserLoginVo;
import com.atguigu.gulimall.authserver.vo.UserRegisterVo;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * @author lxy
 * @version 1.0
 * @Description
 * @date 2023/5/7 16:42
 */
@FeignClient("gulimall-member")
public interface MemberFeignService {
    /**
     * 会员注册
     * @param vo
     * @return
     */
    @PostMapping("/member/member/register")
    R register(@RequestBody UserRegisterVo vo);

    /**
     * 会员登录
     * @param vo
     * @return
     */
    @PostMapping("/member/member/login")
    R login(@RequestBody UserLoginVo vo);

    /**
     * 微博登录【社交登录】
     * @param vo
     * @return
     */
    @PostMapping("/member/member/oauth2/weibo/login")
    R weiboLogin(@RequestBody SocialWeiBoAuthInfo vo);
}
