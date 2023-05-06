package com.atguigu.gulimall.authserver.utils;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * @author lxy
 * @version 1.0
 * @Description 腾讯云SMS配置类
 * @date 2023/5/6 14:50
 */
@ConfigurationProperties(prefix = "tencent.sms")
@Component
@Data
public class TencentSmsConfigProperties {
    /**
     * 秘钥id
     */
    private String secretId;
    /**
     * 秘钥key
     */
    private String secretKey;
    /**
     * AppId
     */
    private String sdkAppId;
    /**
     * 模板id
     */
    private String templateCode;
    /**
     * 签名内容
     */
    private String signName;
}
