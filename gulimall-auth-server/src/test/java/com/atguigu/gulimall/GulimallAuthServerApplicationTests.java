package com.atguigu.gulimall;

import com.atguigu.gulimall.utils.ConstantPropertiesUtil;
import com.atguigu.gulimall.utils.SmsUtil;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@Slf4j
@RunWith(SpringRunner.class)
@SpringBootTest
public class GulimallAuthServerApplicationTests {

    @Test
    public void contextLoads() {
        System.out.println(ConstantPropertiesUtil.APP_CODE);
        // 腾讯SMS
        // SendSmsByTencent.sendSms("18625983574","1613565","123456","10");
        // 国阳云SMS
        // SmsUtil.sendMessage(ConstantPropertiesUtil.APP_CODE,"18625983574","156652","30",ConstantPropertiesUtil.SMS_SIGNID,ConstantPropertiesUtil.TEMPLATE_ID);
        // 邮箱发送
        // MailUtils.sendMail("2422737092@qq.com", "[谷粒学院] 验证码: \n"+code+"您正在进行注册,若非本人操作,请勿泄露.30分钟内有效.", "谷粒学院验证码");
    }

}
