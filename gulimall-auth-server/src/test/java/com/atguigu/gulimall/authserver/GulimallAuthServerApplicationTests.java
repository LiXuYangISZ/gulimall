package com.atguigu.gulimall.authserver;

import com.atguigu.gulimall.authserver.utils.*;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@Slf4j
@RunWith(SpringRunner.class)
@SpringBootTest
public class GulimallAuthServerApplicationTests {

    @Autowired
    TencentSmsConfigProperties tencentSmsConfigProperties;

    @Autowired
    TencentSms tencentSms;

    @Autowired
    GYSms gySms;

    @Autowired
    GYConfigProperties gyConfigProperties;

    @Test
    public void contextLoads() {
        // 腾讯SMS
        // System.out.println(tencentSmsConfigProperties.getSdkAppId());
        // tencentSms.sendSms("18625983574","123456","10");
        // 国阳云SMS
        // gySms.sendMessage("18625983574","156652","30");
        // System.out.println(gyConfigProperties.getAppCode());
        // 邮箱发送
        MailUtils.sendMail("2422737092@qq.com", "[谷粒学院] 验证码: \n"+"584624"+"您正在进行注册,若非本人操作,请勿泄露.30分钟内有效.", "谷粒学院验证码");
    }

}
