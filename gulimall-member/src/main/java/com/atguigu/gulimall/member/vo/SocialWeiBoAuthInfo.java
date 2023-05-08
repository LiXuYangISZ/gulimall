package com.atguigu.gulimall.member.vo;

import lombok.Data;

/**
 * @author lxy
 * @version 1.0
 * @Description
 * @date 2023/5/8 15:05
 */
@Data
public class SocialWeiBoAuthInfo {
    private String access_token;
    private String remind_in;
    private Long expires_in;
    private String uid;
    private Boolean isRealName;
}
