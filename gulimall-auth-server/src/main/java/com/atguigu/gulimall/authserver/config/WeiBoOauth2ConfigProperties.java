package com.atguigu.gulimall.authserver.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * @author lxy
 * @version 1.0
 * @Description 微博社交登录相关参数配置类
 * @date 2023/5/8 14:11
 */

@Data
@Component
@ConfigurationProperties(prefix = "oauth2.weibo")
public class WeiBoOauth2ConfigProperties {
    private String clientId;
    private String clientSecret;
    private String grantType;
    private String redirectUri;
}
