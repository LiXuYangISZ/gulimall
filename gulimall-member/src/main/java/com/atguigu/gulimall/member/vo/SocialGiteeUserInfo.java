package com.atguigu.gulimall.member.vo;

import lombok.Data;

/**
 * @author lxy
 * @version 1.0
 * @Description Gitee用户信息
 * @date 2023/5/9 12:24
 */
@Data
public class SocialGiteeUserInfo {
    /**
     * 用户id
     */
    private Long id;
    /**
     * 用户昵称
     */
    private String login;
    /**
     * 用户姓名
     */
    private String name;
    /**
     * 头像
     */
    private String avatar_url;
    /**
     * 描述【个性签名】
     */
    private String bio;
}
