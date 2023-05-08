package com.atguigu.common.constant.member;

/**
 * @author lxy
 * @version 1.0
 * @Description
 * @date 2023/5/8 21:11
 */
public enum SocialTypeEnum {
    TYPE_WEIBO(0,"微博"),
    TYPE_GITEE(1,"Gitee"),
    TYPE_WECHAT(2,"微信"),
    TYPE_QQ(3,"QQ"),
    TYPE_GITHUB(4,"Github");
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
