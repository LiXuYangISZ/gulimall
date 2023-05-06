package com.atguigu.gulimall.authserver.utils;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * @author lxy
 * @version 1.0
 * @Description
 * @date 2023/5/6 15:41
 */
@Component
public class MailConfigProperties implements InitializingBean {
    @Value("${mail.user}")
    private String user;
    @Value("${mail.password}")
    private String password;

    /**
     * 邮箱号
     */
    public static String USER;
    /**
     * 授权码
     */
    public static String PASSWORD;

    @Override
    public void afterPropertiesSet() throws Exception {
        USER = user;
        PASSWORD = password;
    }
}

