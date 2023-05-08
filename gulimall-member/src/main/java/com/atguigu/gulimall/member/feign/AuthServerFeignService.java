package com.atguigu.gulimall.member.feign;

import com.atguigu.common.utils.R;
import com.atguigu.gulimall.member.vo.SocialWeiBoAuthInfo;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * @author lxy
 * @version 1.0
 * @Description
 * @date 2023/5/8 22:20
 */
@FeignClient("gulimall-auth-server")
public interface AuthServerFeignService {
    /**
     * 根据access_tocken获取微博用户的基本信息【昵称、头像、性别、地址】
     * @param socialWeiBoUser
     * @return
     * @throws Exception
     */
    @PostMapping("/oauth2.0/weibo/getUserInfo")
    R getWeiBoUserInfo(@RequestBody SocialWeiBoAuthInfo socialWeiBoUser) throws Exception;
}
