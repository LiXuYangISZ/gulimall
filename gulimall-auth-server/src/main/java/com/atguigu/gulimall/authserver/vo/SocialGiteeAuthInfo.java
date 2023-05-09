package com.atguigu.gulimall.authserver.vo;

import lombok.Data;

/**
 * @author lxy
 * @version 1.0
 * @Description
 * @date 2023/5/8 16:11
 */
@Data
public class SocialGiteeAuthInfo {
    private String access_token;
    private String token_type;
    private Long expires_in;
    private String refresh_token;
    private String scope;
    private Long created_at;
}
