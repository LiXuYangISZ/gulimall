package com.atguigu.gulimall.authserver.controller;

import com.atguigu.common.constant.authserver.AuthServerConstant;
import com.atguigu.common.exception.BizCodeEnum;
import com.atguigu.common.utils.R;
import com.atguigu.common.utils.RandomUtil;
import com.atguigu.gulimall.authserver.feign.ThirdPartFeignService;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.concurrent.TimeUnit;

/**
 * @author lxy
 * @version 1.0
 * @Description
 * @date 2023/5/5 13:59
 */
@Controller
public class IndexController {

    @Autowired
    ThirdPartFeignService thirdPartFeignService;

    @Autowired
    StringRedisTemplate redisTemplate;

    @ResponseBody
    @GetMapping("/sms/sendCode")
    public R sendCode(@RequestParam("phone") String phone){
        // 1.接口防刷

        // 2.验证码的前校验【为了防止一个用户1min内大量获取验证码】
        String redisCode = redisTemplate.opsForValue().get(AuthServerConstant.SMS_CODE_CACHE_PREFIX + phone);
        if(StringUtils.isNotBlank(redisCode)){
            Long time = Long.valueOf(redisCode.split("_")[1]);
            if(System.currentTimeMillis() - time < 30000){
                return R.error(BizCodeEnum.SMS_CODE_EXCEPTION.getCode(),BizCodeEnum.SMS_CODE_EXCEPTION.getMessage());
            }
        }
        String code = RandomUtil.getSixBitRandom();
        redisTemplate.opsForValue().set(AuthServerConstant.SMS_CODE_CACHE_PREFIX+phone,code+"_"+System.currentTimeMillis(),AuthServerConstant.SMS_CODE_EXPIRATION_TIME, TimeUnit.MINUTES);
        thirdPartFeignService.sendCode(phone, code,AuthServerConstant.SMS_CODE_EXPIRATION_TIME.toString());
        return R.ok();
    }


}
