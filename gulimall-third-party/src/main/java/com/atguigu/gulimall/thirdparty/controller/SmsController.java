package com.atguigu.gulimall.thirdparty.controller;

import com.atguigu.common.utils.R;
import com.atguigu.gulimall.thirdparty.component.TencentSmsComponent;
import com.atguigu.gulimall.thirdparty.util.MailUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author lxy
 * @version 1.0
 * @Description
 * @date 2023/5/6 17:06
 */
@RestController
@RequestMapping("/sms")
public class SmsController {

    @Autowired
    TencentSmsComponent tencentSmsComponent;

    /**
     * 提供给别的服务进行调用的
     * @param phone
     * @param code
     * @param minute
     * @return
     */
    @GetMapping("/sendCode")
    public R sendCode(@RequestParam("phone") String phone,@RequestParam("code") String code,@RequestParam("minute") String minute){
        tencentSmsComponent.sendSms(phone,code,minute);
        // MailUtils.sendMail(phone,"您的注册验证码为 <b><font color=blue>"+code+"</font><b>，请在页面中输入完成验证。为保障您的帐户安全，请在"+minute+"分钟内完成验证，否则验证码将自动失效。\n","谷粒商城注册验证码");
        System.out.println("验证码发送成功:"+code);
        return R.ok();
    }
}
