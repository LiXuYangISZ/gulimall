package com.atguigu.common.constant.member;

/**
 * @author lxy
 * @version 1.0
 * @Description 社交账号类型
 * @date 2023/5/8 21:11
 */
public enum SocialTypeEnum {
    /**
     * 原生：用户名密码的方式
     */
    TYPE_NONE(0,"原生"),
    TYPE_WEIBO(1,"微博"),
    TYPE_GITEE(2,"Gitee"),
    TYPE_WECHAT(3,"微信"),
    TYPE_QQ(4,"QQ"),
    TYPE_GITHUB(5,"Github");
    private Integer code;
    private String msg;

    SocialTypeEnum(Integer code, String msg) {
        this.code = code;
        this.msg = msg;
    }

    public Integer getCode() {
        return code;
    }

    public String getMsg() {
        return msg;
    }
}
