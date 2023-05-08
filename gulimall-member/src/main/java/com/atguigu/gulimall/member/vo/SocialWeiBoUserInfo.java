package com.atguigu.gulimall.member.vo;

import lombok.Data;

/**
 * @author lxy
 * @version 1.0
 * @Description 微博用户基本信息
 * @date 2023/5/8 22:09
 */
@Data
public class SocialWeiBoUserInfo {
    /**
     * 用户id
     */
    private Long id;
    /**
     * 用户名【昵称】
     */
    private String name;
    /**
     * 位置【对应member中的city】
     */
    private String location;
    /**
     * 性别【m:男性，f：女性】
     */
    private String gender;
    /**
     * 头像
     */
    private String avatar_hd;
    /**
     * 描述【个性签名】
     */
    private String description;
}
