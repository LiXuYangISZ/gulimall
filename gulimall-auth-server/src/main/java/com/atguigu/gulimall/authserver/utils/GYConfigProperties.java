package com.atguigu.gulimall.authserver.utils;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * @author lxy
 * @version 1.0
 * @Description 国阳云短信服务配置类
 * @date 2022/3/8 15:29
 */
@ConfigurationProperties(prefix = "gyyun.sms")
@Component
@Data
public class GYConfigProperties{
    /**
     * appCode
     */
    private String appCode;
    /**
     * 签名id
     */
    private String smsSignid;
    /**
     * 模板id
     */
    private String templateId;
}
